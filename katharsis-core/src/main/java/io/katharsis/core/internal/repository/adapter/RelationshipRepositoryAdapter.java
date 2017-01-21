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
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.filter.RepositoryFilterContext;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.BulkRelationshipRepositoryV2;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.RepositoryRequestSpec;
import io.katharsis.repository.response.JsonApiResponse;
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
	public JsonApiResponse setRelation(T source, J targetId, String fieldName, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Serializable targetId = request.getId();
				String fieldName = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		        if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .setRelation(source, targetId, fieldName, queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).setRelation(source, targetId, fieldName);
		        } else {
		            ((RelationshipRepository) relationshipRepository).setRelation(source, targetId, fieldName);
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry.getResourceRegistry(), HttpMethod.PATCH, source, queryAdapter, Arrays.asList(targetId), fieldName, resourceInformation.getResourceClass());
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse setRelations(T source, Iterable<J> targetIds, String fieldName, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				String fieldName = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		        if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .setRelations(source, targetIds, fieldName, queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).setRelations(source, targetIds, fieldName);
		        } else {
		            ((RelationshipRepository) relationshipRepository).setRelations(source, targetIds, fieldName);
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry.getResourceRegistry(), HttpMethod.PATCH, source, queryAdapter, targetIds, fieldName, resourceInformation.getResourceClass());
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse addRelations(T source, Iterable<J> targetIds, String fieldName, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				String fieldName = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		        if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .addRelations(source, targetIds, fieldName, queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).addRelations(source, targetIds, fieldName);
		        } else {
		            ((RelationshipRepository) relationshipRepository).addRelations(source, targetIds, fieldName);
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry.getResourceRegistry(), HttpMethod.POST, source, queryAdapter, targetIds, fieldName, resourceInformation.getResourceClass());
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse removeRelations(T source, Iterable<J> targetIds, String fieldName, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				String fieldName = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
		    	if (isAnnotated) {
		            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .removeRelations(source, targetIds, fieldName, queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	((RelationshipRepositoryV2)relationshipRepository).removeRelations(source, targetIds, fieldName);
		        } else {
		            ((RelationshipRepository) relationshipRepository).removeRelations(source, targetIds, fieldName);
		        }
		        return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry.getResourceRegistry(), HttpMethod.DELETE, source, queryAdapter, targetIds, fieldName, resourceInformation.getResourceClass());
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

	@SuppressWarnings("rawtypes")
	public JsonApiResponse findOneTarget(I sourceId, String fieldName, QueryAdapter queryAdapter) {
    	RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Serializable sourceId = request.getId();
				String fieldName = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				
		    	Object resource;
		        if (isAnnotated) {
		            resource = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .findOneTarget(sourceId, fieldName, queryAdapter);
		        } else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	RelationshipRepositoryV2 querySpecRepository = (RelationshipRepositoryV2) relationshipRepository;
		        	Class<?> targetResourceClass = querySpecRepository.getTargetResourceClass();
		        	resource = querySpecRepository.findOneTarget(sourceId, fieldName, request.getQuerySpec(targetResourceClass));
		        } else {
		            resource = ((RelationshipRepository) relationshipRepository)
		                .findOneTarget(sourceId, fieldName, request.getQueryParams());
		        }
		        return getResponse(relationshipRepository, resource, request);
			};
    	};
    	RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry.getResourceRegistry(), queryAdapter, Arrays.asList(sourceId), fieldName, resourceInformation.getResourceClass());
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }

	@SuppressWarnings("rawtypes")
	public JsonApiResponse findManyTargets(I sourceId, String fieldName, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Serializable sourceId = request.getId();
				String fieldName = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				
				Object resources;
		        if (isAnnotated) {
		            resources = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
		                .findManyTargets(sourceId, fieldName, queryAdapter);
		        }else if(relationshipRepository instanceof RelationshipRepositoryV2){
		        	RelationshipRepositoryV2 querySpecRepository = (RelationshipRepositoryV2) relationshipRepository;
		        	Class<?> targetResourceClass = querySpecRepository.getTargetResourceClass();
		            	resources = querySpecRepository.findManyTargets(sourceId, fieldName, request.getQuerySpec(targetResourceClass));
		        } else {
		            resources = ((RelationshipRepository) relationshipRepository)
		                .findManyTargets(sourceId, fieldName, request.getQueryParams());
		        }
		        return getResponse(relationshipRepository, resources, request);
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry.getResourceRegistry(), queryAdapter, Arrays.asList(sourceId), fieldName, resourceInformation.getResourceClass());
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
    }
	
	@SuppressWarnings("rawtypes")
	public Map<I, JsonApiResponse> findBulkManyTargets(List<I> sourceIds, String fieldName, QueryAdapter queryAdapter) {
		if(relationshipRepository instanceof BulkRelationshipRepositoryV2){
			RepositoryBulkRequestFilterChainImpl<I> chain = new RepositoryBulkRequestFilterChainImpl<I>() {

				@Override
				protected Map<I, JsonApiResponse> invoke(RepositoryFilterContext context) {
					RepositoryRequestSpec request = context.getRequest();
					Iterable<I> sourceIds = request.getIds();
					String fieldName = request.getRelationshipField();
					QueryAdapter queryAdapter = request.getQueryAdapter();
					
					BulkRelationshipRepositoryV2 bulkRepository = (BulkRelationshipRepositoryV2) relationshipRepository;
					Class<?> targetResourceClass = bulkRepository.getTargetResourceClass();
					QuerySpec querySpec = request.getQuerySpec(targetResourceClass);
					MultivaluedMap targetsMap = bulkRepository.findTargets(sourceIds, fieldName, querySpec);
					return toResponses(targetsMap, true, queryAdapter, fieldName, HttpMethod.GET);
				}
			};
			RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry.getResourceRegistry(), queryAdapter, sourceIds, fieldName, resourceInformation.getResourceClass());
			return chain.doFilter(newRepositoryFilterContext(requestSpec));
		}else{
			// fallback to non-bulk operation
			Map<I, JsonApiResponse> responseMap = new HashMap<>();
			for(I sourceId : sourceIds){
				JsonApiResponse response = findManyTargets(sourceId, fieldName, queryAdapter);
				responseMap.put(sourceId, response);
			}
			return responseMap;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map<I, JsonApiResponse> findBulkOneTargets(List<I> sourceIds, String fieldName, QueryAdapter queryAdapter) {
		
		if(relationshipRepository instanceof BulkRelationshipRepositoryV2){
			
			RepositoryBulkRequestFilterChainImpl<I> chain = new RepositoryBulkRequestFilterChainImpl<I>() {

				@Override
				protected Map<I, JsonApiResponse> invoke(RepositoryFilterContext context) {
					RepositoryRequestSpec request = context.getRequest();
					Iterable<?> sourceIds = request.getIds();
					String fieldName = request.getRelationshipField();
					QueryAdapter queryAdapter = request.getQueryAdapter();
					
					BulkRelationshipRepositoryV2 bulkRepository = (BulkRelationshipRepositoryV2) relationshipRepository;
					Class targetResourceClass = bulkRepository.getTargetResourceClass();
					MultivaluedMap<I, D> targetsMap = bulkRepository.findTargets(sourceIds, fieldName, request.getQuerySpec(targetResourceClass));
					return toResponses(targetsMap, false, queryAdapter, fieldName, HttpMethod.GET);
				}
			};
			RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry.getResourceRegistry(), queryAdapter, sourceIds, fieldName, resourceInformation.getResourceClass());
			return chain.doFilter(newRepositoryFilterContext(requestSpec));
		}else{
			// fallback to non-bulk operation
			Map<I, JsonApiResponse> responseMap = new HashMap<>();
			for(I sourceId : sourceIds){
				JsonApiResponse response = findOneTarget(sourceId, fieldName, queryAdapter);
				responseMap.put(sourceId, response);
			}
			return responseMap;
		}
	}
	

	private Map<I, JsonApiResponse> toResponses(MultivaluedMap<I, D> targetsMap, boolean isMany, QueryAdapter queryAdapter, String fieldName, HttpMethod method) {
		Map<I, JsonApiResponse> responseMap = new HashMap<>();
		for(I sourceId : targetsMap.keySet()){
			Object targets = isMany ? targetsMap.getList(sourceId) : targetsMap.getUnique(sourceId);
			RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forRelation(moduleRegistry.getResourceRegistry(), method, null, queryAdapter, Collections.singleton(sourceId), fieldName, resourceInformation.getResourceClass());
			JsonApiResponse response = getResponse(relationshipRepository, targets, requestSpec);
			responseMap.put(sourceId, response);
		}
		return responseMap;
	}

	public Object getRelationshipRepository() {
		return relationshipRepository;
	}
	
	@Override
	protected Class<?> getResourceClass(Object repository) {
		return ((RelationshipRepositoryV2<?,?,?,?>)repository).getTargetResourceClass();
	}
}
