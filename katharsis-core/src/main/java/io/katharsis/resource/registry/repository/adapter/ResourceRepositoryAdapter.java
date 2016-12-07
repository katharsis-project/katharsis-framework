package io.katharsis.resource.registry.repository.adapter;

import java.io.Serializable;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.repository.annotated.AnnotatedResourceRepositoryAdapter;
import io.katharsis.repository.filter.RepositoryFilterContext;
import io.katharsis.request.repository.RepositoryRequestSpec;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.response.JsonApiResponse;

/**
 * A repository adapter for resource repository.
 */
@SuppressWarnings("unchecked")
public class ResourceRepositoryAdapter<T, I extends Serializable> extends ResponseRepositoryAdapter {

	private final Object resourceRepository;

	private final boolean isAnnotated;

	public ResourceRepositoryAdapter(ResourceInformation resourceInformation, ModuleRegistry moduleRegistry,
			Object resourceRepository) {
		super(resourceInformation, moduleRegistry);
		this.resourceRepository = resourceRepository;
		this.isAnnotated = resourceRepository instanceof AnnotatedResourceRepositoryAdapter;
	}

	public JsonApiResponse findOne(I id, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Serializable id = request.getId();
				Object resource;
				if (isAnnotated) {
					resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findOne(id, queryAdapter);
				}
				else if (resourceRepository instanceof QuerySpecResourceRepository) {
					resource = ((QuerySpecResourceRepository) resourceRepository).findOne(id,
							request.getQuerySpec(resourceInformation.getResourceClass()));
				}
				else {
					resource = ((ResourceRepository) resourceRepository).findOne(id, request.getQueryParams());
				}
				return getResponse(resourceRepository, resource, request);
			}

		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindId(moduleRegistry.getResourceRegistry(),
				queryAdapter, id);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public JsonApiResponse findAll(QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Object resources;
				if (isAnnotated) {
					resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(queryAdapter);
				}
				else if (resourceRepository instanceof QuerySpecResourceRepository) {
					QuerySpec querySpec = request.getQuerySpec(resourceInformation.getResourceClass());
					resources = ((QuerySpecResourceRepository) resourceRepository)
							.findAll(querySpec);
				}
				else {
					resources = ((ResourceRepository) resourceRepository).findAll(request.getQueryParams());
				}
				return getResponse(resourceRepository, resources, request);
			}

		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindAll(moduleRegistry.getResourceRegistry(),
				queryAdapter);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public JsonApiResponse findAll(Iterable ids, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Iterable<?> ids = request.getIds();
				Object resources;
				if (isAnnotated) {
					resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(ids, queryAdapter);
				}
				else if (resourceRepository instanceof QuerySpecResourceRepository) {
					resources = ((QuerySpecResourceRepository) resourceRepository).findAll(ids,
							request.getQuerySpec(resourceInformation.getResourceClass()));
				}
				else {
					resources = ((ResourceRepository) resourceRepository).findAll(ids, request.getQueryParams());
				}
				return getResponse(resourceRepository, resources, request);
			}

		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindIds(moduleRegistry.getResourceRegistry(),
				queryAdapter, ids);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public <S extends T> JsonApiResponse update(S entity, QueryAdapter queryAdapter) {
		return save(entity, queryAdapter, HttpMethod.PATCH);
	}

	public <S extends T> JsonApiResponse create(S entity, QueryAdapter queryAdapter) {
		return save(entity, queryAdapter, HttpMethod.POST);
	}

	private <S extends T> JsonApiResponse save(S entity, QueryAdapter queryAdapter, HttpMethod method) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object entity = request.getEntity();

				Object resource;
				if (isAnnotated) {
					resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).save(entity);
				}
				else if (resourceRepository instanceof ResourceRepositoryV2) {
					resource = ((ResourceRepositoryV2) resourceRepository).create(entity);
				}
				else if (resourceRepository instanceof QuerySpecResourceRepository) {
					resource = ((QuerySpecResourceRepository) resourceRepository).save(entity);
				}
				else {
					resource = ((ResourceRepository) resourceRepository).save(entity);
				}
				return getResponse(resourceRepository, resource, request);
			}

		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forSave(moduleRegistry.getResourceRegistry(), method,
				queryAdapter, entity);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public JsonApiResponse delete(I id, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Serializable id = request.getId();
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
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forDelete(moduleRegistry.getResourceRegistry(),
				queryAdapter, id);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public Object getResourceRepository() {
		return resourceRepository;
	}

	@Override
	protected Class<?> getResourceClass(Object repository) {
		return resourceInformation.getResourceClass();
	}
}
