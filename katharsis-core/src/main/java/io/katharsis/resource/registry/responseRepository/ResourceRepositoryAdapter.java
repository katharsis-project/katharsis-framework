package io.katharsis.resource.registry.responseRepository;

import java.io.Serializable;
import java.util.Set;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.DefaultQuerySpecConverter;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterOperatorRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotated.AnnotatedResourceRepositoryAdapter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.JsonApiResponse;

/**
 * A repository adapter for resource repository.
 */
@SuppressWarnings("unchecked")
public class ResourceRepositoryAdapter<T, ID extends Serializable> extends ResponseRepository {

    private final Object resourceRepository;
    private final boolean isAnnotated;

    public ResourceRepositoryAdapter(ResourceInformation resourceInformation, ResourceRegistry resourceRegistry, Object resourceRepository) {
    	super(resourceInformation, resourceRegistry);
        this.resourceRepository = resourceRepository;
        this.isAnnotated = resourceRepository instanceof AnnotatedResourceRepositoryAdapter;
    }

    public JsonApiResponse findOne(ID id, QueryAdapter queryAdapter) {
        Object resource;
        if (isAnnotated) {
            resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findOne(id, toQueryParams(queryAdapter));
        } else if(resourceRepository instanceof QuerySpecResourceRepository){
        	resource = ((QuerySpecResourceRepository)resourceRepository).findOne(id, toQuerySpec(queryAdapter));
        } else {
            resource = ((ResourceRepository) resourceRepository).findOne(id, toQueryParams(queryAdapter));
        }
        return getResponse(resourceRepository, resource, queryAdapter);
    }

    public JsonApiResponse findAll(QueryAdapter queryAdapter) {
        Object resources;
        if (isAnnotated) {
            resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(toQueryParams(queryAdapter));
        } else if(resourceRepository instanceof QuerySpecResourceRepository){
        	resources = ((QuerySpecResourceRepository)resourceRepository).findAll(toQuerySpec(queryAdapter));
        } else {
            resources = ((ResourceRepository) resourceRepository).findAll(toQueryParams(queryAdapter));
        }
        return getResponse(resourceRepository, resources, queryAdapter);
    }

    public JsonApiResponse findAll(Iterable ids, QueryAdapter queryAdapter) {
        Object resources;
        if (isAnnotated) {
            resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(ids, toQueryParams(queryAdapter));
        } else if(resourceRepository instanceof QuerySpecResourceRepository){
        	resources = ((QuerySpecResourceRepository)resourceRepository).findAll(ids, toQuerySpec(queryAdapter));
        } else {
            resources = ((ResourceRepository) resourceRepository).findAll(ids, toQueryParams(queryAdapter));
        }
        return getResponse(resourceRepository, resources, queryAdapter);
    }

	public <S extends T> JsonApiResponse save(S entity, QueryAdapter queryAdapter) {
        Object resource;
        if (isAnnotated) {
            resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).save(entity);
        } else if(resourceRepository instanceof QuerySpecResourceRepository){
        	resource = ((QuerySpecResourceRepository)resourceRepository).save(entity);
        }else{
            resource = ((ResourceRepository) resourceRepository).save(entity);
        }
        return getResponse(resourceRepository, resource, queryAdapter);
    }

    public JsonApiResponse delete(ID id, QueryAdapter queryAdapter) {
        if (isAnnotated) {
            ((AnnotatedResourceRepositoryAdapter) resourceRepository).delete(id, toQueryParams(queryAdapter));
        } else if(resourceRepository instanceof QuerySpecResourceRepository){
        	((QuerySpecResourceRepository) resourceRepository).delete(id);
        } else {
            ((ResourceRepository) resourceRepository).delete(id);
        }
        return new JsonApiResponse();
    }

	public Object getResourceRepository() {
		return resourceRepository;
	}

	public FilterOperator getDefaultOperator() {
        if (isAnnotated) {
           throw new UnsupportedOperationException("not implemented yet");
        } else {
           return ((QuerySpecResourceRepository<?,?>) resourceRepository).getDefaultOperator();
        }
	}

	public Set<FilterOperator> getSupportedOperators() {
		if (isAnnotated) {
			throw new UnsupportedOperationException("not implemented yet");
		} else {
			return ((QuerySpecResourceRepository<?,?>) resourceRepository).getSupportedOperators();
		}
	}
}
