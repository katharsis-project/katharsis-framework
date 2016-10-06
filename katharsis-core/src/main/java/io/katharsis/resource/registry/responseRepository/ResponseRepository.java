package io.katharsis.resource.registry.responseRepository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.DefaultQuerySpecConverter;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecLinksRepository;
import io.katharsis.queryspec.QuerySpecMetaRepository;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.repository.annotated.AnnotatedRepositoryAdapter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.paging.DefaultPagedLinksInformation;
import io.katharsis.response.paging.PagedLinksInformation;
import io.katharsis.response.paging.PagedResultList;
import io.katharsis.utils.JsonApiUrlBuilder;

import java.util.Collections;

/**
 * The adapter is used to create a common layer between controllers and repositories. Every repository can return either
 * a resource object or a {@link JsonApiResponse} response which should be returned by a controller. Ok, the last
 * sentence is not 100% true since interface based repositories can return only resources, but who's using it anyway?
 * <p>
 * The methods need to know if a repository is interface- or annotation-based since repository methods have different
 * signatures.
 */
public abstract class ResponseRepository {

    protected ResourceInformation resourceInformation;

    protected ResourceRegistry resourceRegistry;

    public ResponseRepository(ResourceInformation resourceInformation, ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
        this.resourceInformation = resourceInformation;
    }

    @SuppressWarnings("rawtypes")
	protected JsonApiResponse getResponse(Object repository, Object resource, RequestSpec requestSpec) {
        if (resource instanceof JsonApiResponse) {
            return (JsonApiResponse) resource;
        }

        Iterable resources;
        if (resource instanceof Iterable) {
            resources = (Iterable) resource;
        } else {
            resources = Collections.singletonList(resource);
        }
        MetaInformation metaInformation = getMetaInformation(repository, resources, requestSpec);
        LinksInformation linksInformation = getLinksInformation(repository, resources, requestSpec);

        return new JsonApiResponse().setEntity(resource).setLinksInformation(linksInformation)
                .setMetaInformation(metaInformation);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private MetaInformation getMetaInformation(Object repository, Iterable<?> resources, RequestSpec requestSpec) {
    	QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
        if (repository instanceof AnnotatedRepositoryAdapter) {
            if (((AnnotatedRepositoryAdapter) repository).metaRepositoryAvailable()) {
                return ((AnnotatedRepositoryAdapter) repository).getMetaInformation(resources, queryAdapter);
            }
        } else if (repository instanceof QuerySpecMetaRepository) {
            return ((QuerySpecMetaRepository) repository).getMetaInformation(resources, toQuerySpec(queryAdapter, getResourceClass(repository)));
        } else if (repository instanceof MetaRepository) {
            return ((MetaRepository) repository).getMetaInformation(resources, toQueryParams(queryAdapter));
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private LinksInformation getLinksInformation(Object repository, Iterable<?> resources, RequestSpec requestSpec) {
    	QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
    	LinksInformation linksInformation = null;
        if (repository instanceof AnnotatedRepositoryAdapter) {
            if (((AnnotatedRepositoryAdapter) repository).linksRepositoryAvailable()) {
            	linksInformation = ((LinksRepository) repository).getLinksInformation(resources, toQueryParams(queryAdapter));
            }
        } else if (repository instanceof QuerySpecLinksRepository) {
        	linksInformation = ((QuerySpecLinksRepository) repository).getLinksInformation(resources, toQuerySpec(queryAdapter, getResourceClass(repository)));
        } else if (repository instanceof LinksRepository) {
        	linksInformation = ((LinksRepository) repository).getLinksInformation(resources, toQueryParams(queryAdapter));
        }
        return enrichLinksInformation(linksInformation, resources, requestSpec);
    }

    private LinksInformation enrichLinksInformation(LinksInformation linksInformation, Iterable<?> resources, RequestSpec requestSpec) {
    	// TOOD QueryParamsAdapter currently not supported
    	// QueryParamsAdapter.duplicate and the other paging mechanism next to offset/limit need to be implemented
    	QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
    	LinksInformation enrichedLinksInformation = linksInformation;
    	if(queryAdapter instanceof QuerySpecAdapter){
    		enrichedLinksInformation = enrichPageLinksInformation(enrichedLinksInformation, resources, queryAdapter, requestSpec);
    	}
   		return enrichedLinksInformation;
	}

    private LinksInformation enrichPageLinksInformation(LinksInformation linksInformation, Iterable<?> resources,
			QueryAdapter queryAdapter, RequestSpec requestSpec) {
    	if(!(resources instanceof PagedResultList) || queryAdapter.getLimit() == null){
    		return linksInformation;
    	}
    
		if(linksInformation != null && !(linksInformation instanceof PagedLinksInformation)){
			throw new IllegalStateException(linksInformation + " must implement " + PagedLinksInformation.class.getName() + " to support pagination link computation with " + PagedResultList.class);
		}
		
		// only enrich if not already set
		PagedLinksInformation pagedLinksInformation = (PagedLinksInformation) linksInformation;
		if(pagedLinksInformation == null){
			// use default implementation if no link information provided by repository
			pagedLinksInformation = new DefaultPagedLinksInformation();
		}
		if(!hasPageLinks(pagedLinksInformation)){
			PagedResultList<?> pageResultList = (PagedResultList<?>) resources;
			doEnrichPageLinksInformation(pagedLinksInformation, pageResultList, queryAdapter, requestSpec);
		}
		return pagedLinksInformation;
	}

	private boolean hasPageLinks(PagedLinksInformation pagedLinksInformation) {
		return pagedLinksInformation.getFirst() != null || pagedLinksInformation.getLast() != null || pagedLinksInformation.getPrev() != null || pagedLinksInformation.getNext() != null;
	}

	private void doEnrichPageLinksInformation(PagedLinksInformation pagedLinksInformation, PagedResultList<?> pageResultList,
			QueryAdapter queryAdapter, RequestSpec requestSpec) {
    	long total = pageResultList.getTotalCount();
		long pageSize = queryAdapter.getLimit().longValue();
		long offset = queryAdapter.getOffset();

		long currentPage = offset / pageSize;
		if (currentPage * pageSize != offset) {
			throw new IllegalArgumentException("offset " + offset + " is not a multiple of limit " + pageSize);
		}
		long totalPages = (total + pageSize - 1) / pageSize;

		QueryAdapter pageSpec = queryAdapter.duplicate();
		pageSpec.setLimit(pageSize);

		pageSpec.setOffset(0);
		pagedLinksInformation.setFirst(toUrl(pageSpec, pageResultList, requestSpec));

		pageSpec.setOffset((totalPages - 1) * pageSize);
		pagedLinksInformation.setLast(toUrl(pageSpec, pageResultList, requestSpec));

		if (currentPage > 0) {
			pageSpec.setOffset((currentPage - 1) * pageSize);
			pagedLinksInformation.setPrev(toUrl(pageSpec, pageResultList, requestSpec));
		}

		if (currentPage < totalPages - 1) {
			pageSpec.setOffset((currentPage + 1) * pageSize);
			pagedLinksInformation.setNext(toUrl(pageSpec, pageResultList, requestSpec));
		}		
	}
	
	/**
	 * Add some point maybe a more prominent api is necessary for this. But i likely should
	 * be keept separate from QuerySpec.
	 */
	class RequestSpec{
		
		private Object relationshipSourceId;
		private String relationshipField;
		private Class<?> relationshipSourceClass;
		
		private QueryAdapter queryAdapter;
		
		public RequestSpec(QueryAdapter queryAdapter, Object relationshipSourceId, String relationshipField, Class<?> relationshipSourceClass) {
			super();
			this.queryAdapter = queryAdapter;
			this.relationshipSourceId = relationshipSourceId;
			this.relationshipField = relationshipField;
			this.relationshipSourceClass = relationshipSourceClass;
		}

		public RequestSpec(QueryAdapter queryAdapter) {
			this.queryAdapter = queryAdapter;
		}

		public QueryAdapter getQueryAdapter() {
			return queryAdapter;
		}

		public Object getRelationshipSourceId() {
			return relationshipSourceId;
		}
		
		public String getRelationshipField() {
			return relationshipField;
		}
		
		public Class<?> getRelationshipSourceClass() {
			return relationshipSourceClass;
		}
	}
	

	private <T> String toUrl(QueryAdapter queryAdapter, PagedResultList<T> pagedResultList, RequestSpec requestSpec) {
		JsonApiUrlBuilder urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
		Object relationshipSourceId = requestSpec.getRelationshipSourceId();
		String relationshipField = requestSpec.getRelationshipField();
		Class<?> rootClass;
		if (relationshipField == null) {
			rootClass = queryAdapter.getResourceClass();
		}
		else {
			rootClass = requestSpec.getRelationshipSourceClass();
		}
		return urlBuilder.buildUrl(rootClass, relationshipSourceId, queryAdapter, relationshipField);
	}
    
	protected abstract Class<?> getResourceClass(Object repository);

    protected QueryParams toQueryParams(QueryAdapter queryAdapter) {
        if (queryAdapter == null)
            return null;
        return ((QueryParamsAdapter) queryAdapter).getQueryParams();
    }

    protected QuerySpec toQuerySpec(QueryAdapter queryAdapter, Class<?> targetResourceClass) {
        if (queryAdapter == null)
            return null;
        if (queryAdapter instanceof QuerySpecAdapter) {
             QuerySpec querySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
             return querySpec.getOrCreateQuerySpec(targetResourceClass);
        }
        QueryParams queryParams = toQueryParams(queryAdapter);

        DefaultQuerySpecConverter converter = new DefaultQuerySpecConverter(resourceRegistry);

        return converter.fromParams(targetResourceClass, queryParams);
    }
}
