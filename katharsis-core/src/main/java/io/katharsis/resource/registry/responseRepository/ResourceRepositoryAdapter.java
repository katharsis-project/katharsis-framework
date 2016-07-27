package io.katharsis.resource.registry.responseRepository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotated.AnnotatedResourceRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;

import java.io.Serializable;

/**
 * A repository adapter for resource repository.
 */
@SuppressWarnings("unchecked")
public class ResourceRepositoryAdapter<T, ID extends Serializable> extends ResponseRepository {

    private final Object resourceRepository;
    private final boolean isAnnotated;

    public ResourceRepositoryAdapter(Object resourceRepository) {
        this.resourceRepository = resourceRepository;
        this.isAnnotated = resourceRepository instanceof AnnotatedResourceRepositoryAdapter;
    }

    public JsonApiResponse findOne(ID id, QueryParams queryParams) {
        Object resource;
        if (isAnnotated) {
            resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findOne(id, queryParams);
        } else {
            resource = ((ResourceRepository) resourceRepository).findOne(id, queryParams);
        }
        return getResponse(resourceRepository, resource, queryParams);
    }

    public JsonApiResponse findAll(QueryParams queryParams) {
        Object resources;
        if (isAnnotated) {
            resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(queryParams);
        } else {
            resources = ((ResourceRepository) resourceRepository).findAll(queryParams);
        }
        return getResponse(resourceRepository, resources, queryParams);
    }

    public JsonApiResponse findAll(Iterable ids, QueryParams queryParams) {
        Object resources;
        if (isAnnotated) {
            resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(ids, queryParams);
        } else {
            resources = ((ResourceRepository) resourceRepository).findAll(ids, queryParams);
        }
        return getResponse(resourceRepository, resources, queryParams);
    }

    public <S extends T> JsonApiResponse save(S entity, QueryParams queryParams) {
        Object resource;
        if (isAnnotated) {
            resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).save(entity);
        } else {
            resource = ((ResourceRepository) resourceRepository).save(entity);
        }
        return getResponse(resourceRepository, resource, queryParams);
    }

    public JsonApiResponse delete(ID id, QueryParams queryParams) {
        if (isAnnotated) {
            ((AnnotatedResourceRepositoryAdapter) resourceRepository).delete(id, queryParams);
        } else {
            ((ResourceRepository) resourceRepository).delete(id);
        }
        return new JsonApiResponse();
    }
}
