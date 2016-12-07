package io.katharsis.resource.registry.repository.adapter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpecLinksRepository;
import io.katharsis.queryspec.QuerySpecMetaRepository;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.repository.annotated.AnnotatedRepositoryAdapter;
import io.katharsis.repository.filter.RepositoryBulkRequestFilterChain;
import io.katharsis.repository.filter.RepositoryFilter;
import io.katharsis.repository.filter.RepositoryFilterContext;
import io.katharsis.repository.filter.RepositoryLinksFilterChain;
import io.katharsis.repository.filter.RepositoryMetaFilterChain;
import io.katharsis.repository.filter.RepositoryRequestFilterChain;
import io.katharsis.repository.filter.RepositoryResultFilterChain;
import io.katharsis.request.repository.RepositoryRequestSpec;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.paging.DefaultPagedLinksInformation;
import io.katharsis.response.paging.PagedLinksInformation;
import io.katharsis.response.paging.PagedMetaInformation;
import io.katharsis.response.paging.PagedResultList;
import io.katharsis.utils.JsonApiUrlBuilder;
import io.katharsis.utils.PreconditionUtil;

/**
 * The adapter is used to create a common layer between controllers and repositories. Every repository can return either
 * a resource object or a {@link JsonApiResponse} response which should be returned by a controller. Ok, the last
 * sentence is not 100% true since interface based repositories can return only resources, but who's using it anyway?
 * <p>
 * The methods need to know if a repository is interface- or annotation-based since repository methods have different
 * signatures.
 */
public abstract class ResponseRepositoryAdapter {

	protected ResourceInformation resourceInformation;

	protected ModuleRegistry moduleRegistry;

	public ResponseRepositoryAdapter(ResourceInformation resourceInformation, ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.resourceInformation = resourceInformation;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> Iterable<T> filterResult(Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryResultFilterChainImpl<T> chain = new RepositoryResultFilterChainImpl<>((Iterable) resources);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	protected JsonApiResponse getResponse(Object repository, Object result, RepositoryRequestSpec requestSpec) {
		if (result instanceof JsonApiResponse) {
			return (JsonApiResponse) result;
		}

		Iterable<?> resources;
		boolean isCollection = result instanceof Iterable;
		if (isCollection) {
			resources = (Iterable<?>) result;
		}
		else {
			resources = Collections.singletonList(result);
		}
		Iterable<?> filteredResult = filterResult(resources, requestSpec);
		MetaInformation metaInformation = getMetaInformation(repository, resources, requestSpec);
		LinksInformation linksInformation = getLinksInformation(repository, resources, requestSpec);

		Object resultEntity;
		if (isCollection) {
			resultEntity = filteredResult;
		}
		else {
			Iterator<?> iterator = filteredResult.iterator();
			if (iterator.hasNext()) {
				resultEntity = iterator.next();
				PreconditionUtil.assertFalse("expected unique result", iterator.hasNext());
			}
			else {
				resultEntity = null;
			}
		}

		return new JsonApiResponse().setEntity(resultEntity).setLinksInformation(linksInformation)
				.setMetaInformation(metaInformation);
	}

	private MetaInformation getMetaInformation(Object repository, Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryMetaFilterChainImpl chain = new RepositoryMetaFilterChainImpl(repository);
		return chain.doFilter(newRepositoryFilterContext(requestSpec), resources);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private MetaInformation doGetMetaInformation(Object repository, Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		if (resources instanceof ResourceList) {
			ResourceList<?> resourceList = (ResourceList<?>) resources;
			return resourceList.getMeta();
		}
		QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
		if (repository instanceof AnnotatedRepositoryAdapter) {
			if (((AnnotatedRepositoryAdapter) repository).metaRepositoryAvailable()) {
				return ((AnnotatedRepositoryAdapter) repository).getMetaInformation(resources, queryAdapter);
			}
		}
		else if (repository instanceof QuerySpecMetaRepository) {
			return ((QuerySpecMetaRepository) repository).getMetaInformation(resources,
					requestSpec.getQuerySpec(getResourceClass(repository)));
		}
		else if (repository instanceof MetaRepository) {
			return ((MetaRepository) repository).getMetaInformation(resources, requestSpec.getQueryParams());
		}
		return null;
	}

	private LinksInformation getLinksInformation(Object repository, Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		RepositoryLinksFilterChainImpl chain = new RepositoryLinksFilterChainImpl(repository);
		return chain.doFilter(newRepositoryFilterContext(requestSpec), resources);
	}

	class RepositoryMetaFilterChainImpl implements RepositoryMetaFilterChain {

		protected int filterIndex = 0;

		private Object repository;

		public RepositoryMetaFilterChainImpl(Object repository) {
			this.repository = repository;
		}

		@Override
		public <T> MetaInformation doFilter(RepositoryFilterContext context, Iterable<T> resources) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return doGetMetaInformation(repository, resources, context.getRequest());
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterMeta(context, resources, this);
			}
		}
	}

	class RepositoryLinksFilterChainImpl implements RepositoryLinksFilterChain {

		protected int filterIndex = 0;

		private Object repository;

		public RepositoryLinksFilterChainImpl(Object repository) {
			this.repository = repository;
		}

		@Override
		public <T> LinksInformation doFilter(RepositoryFilterContext context, Iterable<T> resources) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return doGetLinksInformation(repository, resources, context.getRequest());
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterLinks(context, resources, this);
			}
		}
	}

	class RepositoryResultFilterChainImpl<T> implements RepositoryResultFilterChain<T> {

		protected int filterIndex = 0;

		private Iterable<T> result;

		public RepositoryResultFilterChainImpl(Iterable<T> result) {
			this.result = result;
		}

		@Override
		public Iterable<T> doFilter(RepositoryFilterContext context) { // NOSONAR
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return result;
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterResult(context, this);
			}
		}
	}

	protected abstract class RepositoryRequestFilterChainImpl implements RepositoryRequestFilterChain {

		protected int filterIndex = 0;

		@Override
		public JsonApiResponse doFilter(RepositoryFilterContext context) {
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return invoke(context);
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterRequest(context, this);
			}
		}

		protected abstract JsonApiResponse invoke(RepositoryFilterContext context);
	}

	protected abstract class RepositoryBulkRequestFilterChainImpl<K> implements RepositoryBulkRequestFilterChain<K> {

		protected int filterIndex = 0;

		@Override
		public Map<K, JsonApiResponse> doFilter(RepositoryFilterContext context) {
			List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
			if (filterIndex == filters.size()) {
				return invoke(context);
			}
			else {
				RepositoryFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filterBulkRequest(context, this);
			}
		}

		protected abstract Map<K, JsonApiResponse> invoke(RepositoryFilterContext context);
	}

	protected RepositoryFilterContext newRepositoryFilterContext(final RepositoryRequestSpec requestSpec) {
		return new RepositoryFilterContext() {

			@Override
			public RepositoryRequestSpec getRequest() {
				return requestSpec;
			}
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LinksInformation doGetLinksInformation(Object repository, Iterable<?> resources, RepositoryRequestSpec requestSpec) {
		if (resources instanceof ResourceList) {
			ResourceList<?> resourceList = (ResourceList<?>) resources;
			boolean createLinksInformation = resourceList instanceof DefaultResourceList;
			LinksInformation newLinksInfo = enrichLinksInformation(resourceList.getLinks(), resources, requestSpec,
					createLinksInformation);
			if (createLinksInformation) {
				((DefaultResourceList) resources).setLinks(newLinksInfo);
			}
			return resourceList.getLinks();
		}

		LinksInformation linksInformation = null;
		if (repository instanceof AnnotatedRepositoryAdapter) {
			if (((AnnotatedRepositoryAdapter) repository).linksRepositoryAvailable()) {
				linksInformation = ((LinksRepository) repository).getLinksInformation(resources, requestSpec.getQueryParams());
			}
		}
		else if (repository instanceof QuerySpecLinksRepository) {
			linksInformation = ((QuerySpecLinksRepository) repository).getLinksInformation(resources,
					requestSpec.getQuerySpec(getResourceClass(repository)));
		}
		else if (repository instanceof LinksRepository) {
			linksInformation = ((LinksRepository) repository).getLinksInformation(resources, requestSpec.getQueryParams());
		}
		boolean createLinksInformation = true; // backward compatibility, everything deprecated anyway
		return enrichLinksInformation(linksInformation, resources, requestSpec, createLinksInformation);
	}

	private LinksInformation enrichLinksInformation(LinksInformation linksInformation, Iterable<?> resources,
			RepositoryRequestSpec requestSpec, boolean createLinksInformation) {
		// TOOD QueryParamsAdapter currently not supported
		// QueryParamsAdapter.duplicate and the other paging mechanism next to offset/limit need to be implemented
		QueryAdapter queryAdapter = requestSpec.getQueryAdapter();
		LinksInformation enrichedLinksInformation = linksInformation;
		if (queryAdapter instanceof QuerySpecAdapter && (queryAdapter.getOffset() != 0 || queryAdapter.getLimit() != null)) {
			enrichedLinksInformation = enrichPageLinksInformation(enrichedLinksInformation, resources, queryAdapter, requestSpec,
					true);
		}
		return enrichedLinksInformation;
	}

	private LinksInformation enrichPageLinksInformation(LinksInformation linksInformation, Iterable<?> resources,
			QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec, boolean createLinksInformation) {

		if (linksInformation == null && createLinksInformation || linksInformation instanceof PagedLinksInformation) {
			Long totalCount = getTotalCount(resources);
			if (totalCount != null) {
				PagedLinksInformation pagedLinksInformation = (PagedLinksInformation) linksInformation;

				if (pagedLinksInformation == null) {
					// use default implementation if no link information provided by repository
					pagedLinksInformation = new DefaultPagedLinksInformation();
				}

				// only enrich if not already set
				if (!hasPageLinks(pagedLinksInformation)) {
					doEnrichPageLinksInformation(pagedLinksInformation, totalCount, queryAdapter, requestSpec);
				}
				return pagedLinksInformation;
			}
		}
		return linksInformation;
	}

	private Long getTotalCount(Iterable<?> resources) {
		if (resources instanceof PagedResultList) {
			return ((PagedResultList<?>) resources).getTotalCount();
		}
		else if (resources instanceof ResourceList) {
			ResourceList<?> list = (ResourceList<?>) resources;
			PagedMetaInformation pagedMeta = list.getMeta(PagedMetaInformation.class);
			if (pagedMeta != null) {
				return pagedMeta.getTotalResourceCount();
			}
		}
		return null;
	}

	private boolean hasPageLinks(PagedLinksInformation pagedLinksInformation) {
		return pagedLinksInformation.getFirst() != null || pagedLinksInformation.getLast() != null
				|| pagedLinksInformation.getPrev() != null || pagedLinksInformation.getNext() != null;
	}

	private void doEnrichPageLinksInformation(PagedLinksInformation pagedLinksInformation, long total, QueryAdapter queryAdapter,
			RepositoryRequestSpec requestSpec) {
		long pageSize = queryAdapter.getLimit().longValue();
		long offset = queryAdapter.getOffset();

		long currentPage = offset / pageSize;
		if (currentPage * pageSize != offset) {
			throw new IllegalArgumentException("offset " + offset + " is not a multiple of limit " + pageSize);
		}
		long totalPages = (total + pageSize - 1) / pageSize;

		QueryAdapter pageSpec = queryAdapter.duplicate();
		pageSpec.setLimit(pageSize);

		if (totalPages > 0) {
			pageSpec.setOffset(0);
			pagedLinksInformation.setFirst(toUrl(pageSpec, requestSpec));

			pageSpec.setOffset((totalPages - 1) * pageSize);
			pagedLinksInformation.setLast(toUrl(pageSpec, requestSpec));

			if (currentPage > 0) {
				pageSpec.setOffset((currentPage - 1) * pageSize);
				pagedLinksInformation.setPrev(toUrl(pageSpec, requestSpec));
			}

			if (currentPage < totalPages - 1) {
				pageSpec.setOffset((currentPage + 1) * pageSize);
				pagedLinksInformation.setNext(toUrl(pageSpec, requestSpec));
			}
		}
	}

	private <T> String toUrl(QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec) {
		JsonApiUrlBuilder urlBuilder = new JsonApiUrlBuilder(moduleRegistry.getResourceRegistry());
		Object relationshipSourceId = requestSpec.getId();
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

}
