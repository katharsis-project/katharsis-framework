package io.katharsis.resource.registry.responseRepository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.annotated.AnnotatedRelationshipRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;

import java.io.Serializable;

/**
 * A repository adapter for relationship repository.
 */
@SuppressWarnings("unchecked")
public class RelationshipRepositoryAdapter<T, T_ID extends Serializable, D, D_ID extends Serializable>
    extends ResponseRepository {

    private final Object relationshipRepository;
    private final boolean isAnnotated;

    public RelationshipRepositoryAdapter(Object relationshipRepository) {
        this.relationshipRepository = relationshipRepository;
        this.isAnnotated = relationshipRepository instanceof AnnotatedRelationshipRepositoryAdapter;
    }

    public JsonApiResponse setRelation(T source, D_ID targetId, String fieldName, QueryParams queryParams) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .setRelation(source, targetId, fieldName, queryParams);
        } else {
            ((RelationshipRepository) relationshipRepository).setRelation(source, targetId, fieldName);
        }
        return new JsonApiResponse();
    }

    public JsonApiResponse setRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .setRelations(source, targetIds, fieldName, queryParams);
        } else {
            ((RelationshipRepository) relationshipRepository).setRelations(source, targetIds, fieldName);
        }
        return new JsonApiResponse();
    }

    public JsonApiResponse addRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .addRelations(source, targetIds, fieldName, queryParams);
        } else {
            ((RelationshipRepository) relationshipRepository).addRelations(source, targetIds, fieldName);
        }
        return new JsonApiResponse();
    }

    public JsonApiResponse removeRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        if (isAnnotated) {
            ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .removeRelations(source, targetIds, fieldName, queryParams);
        } else {
            ((RelationshipRepository) relationshipRepository).removeRelations(source, targetIds, fieldName);
        }
        return new JsonApiResponse();
    }

    public JsonApiResponse findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams) {
        Object resource;
        if (isAnnotated) {
            resource = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .findOneTarget(sourceId, fieldName, queryParams);
        } else {
            resource = ((RelationshipRepository) relationshipRepository)
                .findOneTarget(sourceId, fieldName, queryParams);
        }
        return getResponse(relationshipRepository, resource, queryParams);
    }

    public JsonApiResponse findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams) {
        Object resources;
        if (isAnnotated) {
            resources = ((AnnotatedRelationshipRepositoryAdapter) relationshipRepository)
                .findManyTargets(sourceId, fieldName, queryParams);
        } else {
            resources = ((RelationshipRepository) relationshipRepository)
                .findManyTargets(sourceId, fieldName, queryParams);
        }
        return getResponse(relationshipRepository, resources, queryParams);
    }
}
