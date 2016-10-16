package io.katharsis.resource.registry.responseRepository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.katharsis.queryspec.QuerySpecBulkRelationshipRepository;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.annotated.AnnotatedRelationshipRepositoryAdapter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.JsonApiResponse;

/**
 * A repository adapter for relationship repository.
 */
@SuppressWarnings("unchecked")
public class RelationshipRepositoryAdapter<T, T_ID extends Serializable, D, D_ID extends Serializable>
    extends ResponseRepository {

    private final Object relationshipRepository;
    private final boolean isAnnotated;

    public RelationshipRepositoryAdapter(ResourceInformation resourceInformation, ResourceRegistry resourceRegistry, Object relationshipRepository) {
    	super(resourceInformation, resourceRegistry);
        this.relationshipRepository = relationshipRepository;
        this.isAnnotated = relationshipRepository instanceof AnnotatedRelationshipRepositoryAdapter;
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse setRelation(T source, D_ID targetId, String fieldName, QueryAdapter queryAdapter) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .setRelation(source, targetId, fieldName, queryAdapter);
        } else if(relationshipRepository instanceof QuerySpecRelationshipRepository){
        	((QuerySpecRelationshipRepository)relationshipRepository).setRelation(source, targetId, fieldName);
        } else {
            ((RelationshipRepository) relationshipRepository).setRelation(source, targetId, fieldName);
        }
        return new JsonApiResponse();
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse setRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryAdapter queryAdapter) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .setRelations(source, targetIds, fieldName, queryAdapter);
        } else if(relationshipRepository instanceof QuerySpecRelationshipRepository){
        	((QuerySpecRelationshipRepository)relationshipRepository).setRelations(source, targetIds, fieldName);
        } else {
            ((RelationshipRepository) relationshipRepository).setRelations(source, targetIds, fieldName);
        }
        return new JsonApiResponse();
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse addRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryAdapter queryAdapter) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .addRelations(source, targetIds, fieldName, queryAdapter);
        } else if(relationshipRepository instanceof QuerySpecRelationshipRepository){
        	((QuerySpecRelationshipRepository)relationshipRepository).addRelations(source, targetIds, fieldName);
        } else {
            ((RelationshipRepository) relationshipRepository).addRelations(source, targetIds, fieldName);
        }
        return new JsonApiResponse();
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse removeRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryAdapter queryAdapter) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .removeRelations(source, targetIds, fieldName, queryAdapter);
        } else if(relationshipRepository instanceof QuerySpecRelationshipRepository){
        	((QuerySpecRelationshipRepository)relationshipRepository).removeRelations(source, targetIds, fieldName);
        } else {
            ((RelationshipRepository) relationshipRepository).removeRelations(source, targetIds, fieldName);
        }
        return new JsonApiResponse();
    }

    @SuppressWarnings("rawtypes")
	public JsonApiResponse findOneTarget(T_ID sourceId, String fieldName, QueryAdapter queryAdapter) {
        Object resource;
        if (isAnnotated) {
            resource = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .findOneTarget(sourceId, fieldName, queryAdapter);
        } else if(relationshipRepository instanceof QuerySpecRelationshipRepository){
        	QuerySpecRelationshipRepository querySpecRepository = (QuerySpecRelationshipRepository) relationshipRepository;
        	Class<?> targetResourceClass = querySpecRepository.getTargetResourceClass();
        	resource = querySpecRepository.findOneTarget(sourceId, fieldName, toQuerySpec(queryAdapter, targetResourceClass));
        } else {
            resource = ((RelationshipRepository) relationshipRepository)
                .findOneTarget(sourceId, fieldName, toQueryParams(queryAdapter));
        }
        RequestSpec requestSpec = new RequestSpec(queryAdapter, sourceId, fieldName, resourceInformation.getResourceClass());
        return getResponse(relationshipRepository, resource, requestSpec);
    }

	@SuppressWarnings("rawtypes")
	public JsonApiResponse findManyTargets(T_ID sourceId, String fieldName, QueryAdapter queryAdapter) {
        Object resources;
        if (isAnnotated) {
            resources = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .findManyTargets(sourceId, fieldName, queryAdapter);
        }else if(relationshipRepository instanceof QuerySpecRelationshipRepository){
        	QuerySpecRelationshipRepository querySpecRepository = (QuerySpecRelationshipRepository) relationshipRepository;
        	Class<?> targetResourceClass = querySpecRepository.getTargetResourceClass();
            	resources = querySpecRepository.findManyTargets(sourceId, fieldName, toQuerySpec(queryAdapter, targetResourceClass));
        } else {
            resources = ((RelationshipRepository) relationshipRepository)
                .findManyTargets(sourceId, fieldName, toQueryParams(queryAdapter));
        }
        RequestSpec requestSpec = new RequestSpec(queryAdapter, sourceId, fieldName, resourceInformation.getResourceClass());
        return getResponse(relationshipRepository, resources, requestSpec);
    }
	
	@SuppressWarnings("rawtypes")
	public Map<T_ID, JsonApiResponse> findBulkManyTargets(List<T_ID> sourceIds, String fieldName, QueryAdapter queryAdapter) {
		if(relationshipRepository instanceof QuerySpecBulkRelationshipRepository){
			QuerySpecBulkRelationshipRepository bulkRepository = (QuerySpecBulkRelationshipRepository) relationshipRepository;
			Class<?> targetResourceClass = bulkRepository.getTargetResourceClass();
			Map<T_ID, Iterable<D>> targetsMap = bulkRepository.findManyTargets(sourceIds, fieldName, toQuerySpec(queryAdapter, targetResourceClass));
			return toResponses(targetsMap, queryAdapter, fieldName);
		}else{
			// fallback to non-bulk operation
			Map<T_ID, JsonApiResponse> responseMap = new HashMap<>();
			for(T_ID sourceId : sourceIds){
				JsonApiResponse response = findManyTargets(sourceId, fieldName, queryAdapter);
				responseMap.put(sourceId, response);
			}
			return responseMap;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map<T_ID, JsonApiResponse> findBulkOneTargets(List<T_ID> sourceIds, String fieldName, QueryAdapter queryAdapter) {
		if(relationshipRepository instanceof QuerySpecBulkRelationshipRepository){
			QuerySpecBulkRelationshipRepository bulkRepository = (QuerySpecBulkRelationshipRepository) relationshipRepository;
			Class<?> targetResourceClass = bulkRepository.getTargetResourceClass();
			Map targetsMap = bulkRepository.findOneTargets(sourceIds, fieldName, toQuerySpec(queryAdapter, targetResourceClass));
			return toResponses(targetsMap, queryAdapter, fieldName);
		}else{
			// fallback to non-bulk operation
			Map<T_ID, JsonApiResponse> responseMap = new HashMap<>();
			for(T_ID sourceId : sourceIds){
				JsonApiResponse response = findOneTarget(sourceId, fieldName, queryAdapter);
				responseMap.put(sourceId, response);
			}
			return responseMap;
		}
	}
	

	private Map<T_ID, JsonApiResponse> toResponses(Map<T_ID, ?> targetsMap, QueryAdapter queryAdapter, String fieldName) {
		Map<T_ID, JsonApiResponse> responseMap = new HashMap<>();
		for(Map.Entry<T_ID, ?> entry : targetsMap.entrySet()){
			T_ID sourceId = entry.getKey();
			Object targets = entry.getValue();
			
			RequestSpec requestSpec = new RequestSpec(queryAdapter, sourceId, fieldName, resourceInformation.getResourceClass());
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
		return ((QuerySpecRelationshipRepository)repository).getTargetResourceClass();
	}
}
