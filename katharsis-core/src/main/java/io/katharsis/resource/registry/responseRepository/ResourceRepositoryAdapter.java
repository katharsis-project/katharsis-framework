package io.katharsis.resource.registry.responseRepository;

import java.io.Serializable;

import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.queryspec.internal.QueryAdapter;
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

	public ResourceRepositoryAdapter(ResourceInformation resourceInformation, ResourceRegistry resourceRegistry,
			Object resourceRepository) {
		super(resourceInformation, resourceRegistry);
		this.resourceRepository = resourceRepository;
		this.isAnnotated = resourceRepository instanceof AnnotatedResourceRepositoryAdapter;
	}

	public JsonApiResponse findOne(ID id, QueryAdapter queryAdapter) {
		Object resource;
		if (isAnnotated) {
			resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findOne(id, queryAdapter);
		}
		else if (resourceRepository instanceof QuerySpecResourceRepository) {
			resource = ((QuerySpecResourceRepository) resourceRepository).findOne(id, toQuerySpec(queryAdapter, resourceInformation.getResourceClass()));
		}
		else {
			resource = ((ResourceRepository) resourceRepository).findOne(id, toQueryParams(queryAdapter));
		}
		return getResponse(resourceRepository, resource, new RequestSpec(queryAdapter));
	}

	public JsonApiResponse findAll(QueryAdapter queryAdapter) {
		Object resources;
		if (isAnnotated) {
			resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(queryAdapter);
		}
		else if (resourceRepository instanceof QuerySpecResourceRepository) {
			resources = ((QuerySpecResourceRepository) resourceRepository)
					.findAll(toQuerySpec(queryAdapter, resourceInformation.getResourceClass()));
		}
		else {
			resources = ((ResourceRepository) resourceRepository).findAll(toQueryParams(queryAdapter));
		}
		return getResponse(resourceRepository, resources, new RequestSpec(queryAdapter));
	}

	public JsonApiResponse findAll(Iterable ids, QueryAdapter queryAdapter) {
		Object resources;
		if (isAnnotated) {
			resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(ids, queryAdapter);
		}
		else if (resourceRepository instanceof QuerySpecResourceRepository) {
			resources = ((QuerySpecResourceRepository) resourceRepository).findAll(ids,
					toQuerySpec(queryAdapter, resourceInformation.getResourceClass()));
		}
		else {
			resources = ((ResourceRepository) resourceRepository).findAll(ids, toQueryParams(queryAdapter));
		}
		return getResponse(resourceRepository, resources, new RequestSpec(queryAdapter));
	}

	public <S extends T> JsonApiResponse save(S entity, QueryAdapter queryAdapter) {
		Object resource;
		if (isAnnotated) {
			resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).save(entity);
		}
		else if (resourceRepository instanceof QuerySpecResourceRepository) {
			resource = ((QuerySpecResourceRepository) resourceRepository).save(entity);
		}
		else {
			resource = ((ResourceRepository) resourceRepository).save(entity);
		}
		return getResponse(resourceRepository, resource, new RequestSpec(queryAdapter));
	}

	public JsonApiResponse delete(ID id, QueryAdapter queryAdapter) {
		if (isAnnotated) {
			((AnnotatedResourceRepositoryAdapter) resourceRepository).delete(id, queryAdapter);
		}
		else if (resourceRepository instanceof QuerySpecResourceRepository) {
			((QuerySpecResourceRepository) resourceRepository).delete(id);
		}
		else {
			((ResourceRepository) resourceRepository).delete(id);
		}
		return new JsonApiResponse();
	}

	public Object getResourceRepository() {
		return resourceRepository;
	}

	@Override
	protected Class<?> getResourceClass(Object repository) {
		return resourceInformation.getResourceClass();
	}
}
