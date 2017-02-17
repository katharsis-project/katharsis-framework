package io.katharsis.core.internal.repository.adapter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.katharsis.core.internal.utils.MultivaluedMap;
import io.katharsis.legacy.internal.AnnotatedRelationshipRepositoryAdapter;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.BulkRelationshipRepositoryV2;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.filter.RepositoryFilterContext;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.request.RepositoryRequestSpec;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;

/**
 * A repository adapter for relationship repository.
 */
@SuppressWarnings("unchecked")
public class RelationshipRepositoryAdapter<T, I extends Serializable, D, J extends Serializable>
    extends ResponseRepositoryAdapter {

    private final Object relationshipRepository;
    private final boolean isAnnotated;

    public RelationshipRepositoryAdapter(ResourceInformation resourceInformation, ModuleRegistry moduleRegistry, Object relationshipRepository) {
    	super(resourceInformation, moduleRegistry);
        this.relationshipRepository = relationshipRepository;
        this.isAnnotated = relationshipRepository instanceof AnnotatedRelationshipRepositoryAdapter;
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse setRelation(T source, J targetId, ResourceField field, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Serializable targetId = request.getId();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		        if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .setRelation(source, targetId, field.getUnderlyingName(), queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).setRelation(source, targetId, field.getUnderlyingName());
		        } else {
		            ((RelationshipRepository) relationshipRepository).setRelation(source, targetId, field.getUnderlyingName());
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.PATCH, source, queryAdapter, Arrays.asList(targetId), field);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse setRelations(T source, Iterable<J> targetIds, ResourceField field, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		        if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .setRelations(source, targetIds, field.getUnderlyingName(), queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).setRelations(source, targetIds, field.getUnderlyingName());
		        } else {
		            ((RelationshipRepository) relationshipRepository).setRelations(source, targetIds, field.getUnderlyingName());
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.PATCH, source, queryAdapter, targetIds, field);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse addRelations(T source, Iterable<J> targetIds, ResourceField field, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		        if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .addRelations(source, targetIds, field.getUnderlyingName(), queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).addRelations(source, targetIds, field.getUnderlyingName());
		        } else {
		            ((RelationshipRepository) relationshipRepository).addRelations(source, targetIds, field.getUnderlyingName());
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.POST, source, queryAdapter, targetIds, field);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse removeRelations(T source, Iterable<J> targetIds, ResourceField field, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		    	if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .removeRelations(source, targetIds, field.getUnderlyingName(), queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).removeRelations(source, targetIds, field.getUnderlyingName());
		        } else {
		            ((RelationshipRepository) relationshipRepository).removeRelations(source, targetIds, field.getUnderlyingName());
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.DELETE, source, queryAdapter, targetIds, field);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

	@SuppressWarnings("rawtypes")
	public JsonApiResponse findOneTarget(I sourceId, ResourceField field, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Serializable sourceId = request.getId();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				
		    	Object resource;
		        if (isAnnotated) {
		            resource = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .findOneTarget(sourceId, field.getUnderlyingName(), queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	RelationshipRepositoryV2 querySpecRepository = (RelationshipRepositoryV2) relationshipRepository;
		        	Class<?> targetResourceClass = querySpecRepository.getTargetResourceClass();
		        	ResourceInformation targetResourceInformation = moduleRegistry.getResourceRegistry().findEntry(targetResourceClass).getResourceInformation();
		        	resource = querySpecRepository.findOneTarget(sourceId, field.getUnderlyingName(), request.getQuerySpec(targetResourceInformation));
		        } else {
		            resource = ((RelationshipRepository) relationshipRepository)
		                .findOneTarget(sourceId, field.getUnderlyingName(), request.getQueryParams());
		        }
		        return getResponse(relationshipRepository, resource, request);
			};
    	};
    	RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, Arrays.asList(sourceId), field);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

	@SuppressWarnings("rawtypes")
	public JsonApiResponse findManyTargets(I sourceId, ResourceField field, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Serializable sourceId = request.getId();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				
				Object resources;
		        if (isAnnotated) {
		            resources = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .findManyTargets(sourceId, field.getUnderlyingName(), queryAdapter);
		        }else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	RelationshipRepositoryV2 querySpecRepository = (RelationshipRepositoryV2) relationshipRepository;
		        	Class<?> targetResourceClass = querySpecRepository.getTargetResourceClass();
		        	ResourceInformation targetResourceInformation = moduleRegistry.getResourceRegistry().findEntry(targetResourceClass).getResourceInformation();
		            	resources = querySpecRepository.findManyTargets(sourceId, field.getUnderlyingName(), request.getQuerySpec(targetResourceInformation));
		        } else {
		            resources = ((RelationshipRepository) relationshipRepository)
		                .findManyTargets(sourceId, field.getUnderlyingName(), request.getQueryParams());
		        }
		        return getResponse(relationshipRepository, resources, request);
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, Arrays.asList(sourceId), field);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }
	
	@SuppressWarnings("rawtypes")
	public Map<I, JsonApiResponse> findBulkManyTargets(List<I> sourceIds, ResourceField field, QueryAdapter queryAdapter) {
		if(relationshipRepository instanceof BulkRelationshipRepositoryV2){
			RepositoryBulkRequestFilterChainImpl<I> chain = new RepositoryBulkRequestFilterChainImpl<I>() {

				@Override
				protected Map<I, JsonApiResponse> invoke(RepositoryFilterContext context) {
					RepositoryRequestSpec request = context.getRequest();
					Iterable<I> sourceIds = request.getIds();
					ResourceField field = request.getRelationshipField();
					QueryAdapter queryAdapter = request.getQueryAdapter();
					
					BulkRelationshipRepositoryV2 bulkRepository = (BulkRelationshipRepositoryV2) relationshipRepository;
					Class<?> targetResourceClass = bulkRepository.getTargetResourceClass();
					ResourceInformation targetResourceInformation = moduleRegistry.getResourceRegistry().findEntry(targetResourceClass).getResourceInformation();
					QuerySpec querySpec = request.getQuerySpec(targetResourceInformation);
					MultivaluedMap targetsMap = bulkRepository.findTargets(sourceIds, field.getUnderlyingName(), querySpec);
					return toResponses(targetsMap, true, queryAdapter, field, HttpMethod.GET);
				}
			};
			RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, sourceIds, field);
			return chain.doFilter(newRepositoryFilterContext(requestSpec));
		}else{
			// fallback to non-bulk operation
			Map<I, JsonApiResponse> responseMap = new HashMap<>();
			for(I sourceId : sourceIds){
				JsonApiResponse response = findManyTargets(sourceId, field, queryAdapter);
				responseMap.put(sourceId, response);
			}
			return responseMap;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map<I, JsonApiResponse> findBulkOneTargets(List<I> sourceIds, ResourceField field, QueryAdapter queryAdapter) {
		
		if(relationshipRepository instanceof BulkRelationshipRepositoryV2){
			
			RepositoryBulkRequestFilterChainImpl<I> chain = new RepositoryBulkRequestFilterChainImpl<I>() {

				@Override
				protected Map<I, JsonApiResponse> invoke(RepositoryFilterContext context) {
					RepositoryRequestSpec request = context.getRequest();
					Iterable<?> sourceIds = request.getIds();
					ResourceField field = request.getRelationshipField();
					QueryAdapter queryAdapter = request.getQueryAdapter();
					
					BulkRelationshipRepositoryV2 bulkRepository = (BulkRelationshipRepositoryV2) relationshipRepository;
					Class targetResourceClass = bulkRepository.getTargetResourceClass();
					ResourceInformation targetResourceInformation = moduleRegistry.getResourceRegistry().findEntry(targetResourceClass).getResourceInformation();
					MultivaluedMap<I, D> targetsMap = bulkRepository.findTargets(sourceIds, field.getUnderlyingName(), request.getQuerySpec(targetResourceInformation));
					return toResponses(targetsMap, false, queryAdapter, field, HttpMethod.GET);
				}
			};
			RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, sourceIds, field);
			return chain.doFilter(newRepositoryFilterContext(requestSpec));
		}else{
			// fallback to non-bulk operation
			Map<I, JsonApiResponse> responseMap = new HashMap<>();
			for(I sourceId : sourceIds){
				JsonApiResponse response = findOneTarget(sourceId, field, queryAdapter);
				responseMap.put(sourceId, response);
			}
			return responseMap;
		}
	}
	

	private Map<I, JsonApiResponse> toResponses(MultivaluedMap<I, D> targetsMap, boolean isMany, QueryAdapter queryAdapter, ResourceField field, HttpMethod method) {
		Map<I, JsonApiResponse> responseMap = new HashMap<>();
		for(I sourceId : targetsMap.keySet()){
			Object targets = isMany ? targetsMap.getList(sourceId) : targetsMap.getUnique(sourceId);
			RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry, method, null, queryAdapter, Collections.singleton(sourceId), field);
			JsonApiResponse response = getResponse(relationshipRepository, targets, requestSpec);
			responseMap.put(sourceId, response);
		}
		return responseMap;
	}

	public Object getRelationshipRepository() {
		return relationshipRepository;
	}
	
	@Override
	protected ResourceInformation getResourceInformation(Object repository) {
		Class<?> clazz = ((RelationshipRepositoryV2<?,?,?,?>)repository).getTargetResourceClass();
		return this.moduleRegistry.getResourceRegistry().findEntry(clazz).getResourceInformation();
	}
}
