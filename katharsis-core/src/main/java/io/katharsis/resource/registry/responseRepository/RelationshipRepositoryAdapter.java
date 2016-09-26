package io.katharsis.resource.registry.responseRepository;

import java.io.Serializable;
import java.util.Set;

import io.katharsis.queryspec.FilterOperator;
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
        return getResponse(relationshipRepository, resource, queryAdapter);
    }

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
        return getResponse(relationshipRepository, resources, queryAdapter);
    }
	

	public Object getRelationshipRepository() {
		return relationshipRepository;
	}
	
	@Override
	protected Class<?> getResourceClass(Object repository) {
		return ((QuerySpecRelationshipRepository)repository).getTargetResourceClass();
	}
}
