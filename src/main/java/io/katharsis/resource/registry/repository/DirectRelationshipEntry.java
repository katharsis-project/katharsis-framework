package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RelationshipRepository;
import net.jodah.typetools.TypeResolver;

public class DirectRelationshipEntry<T, D> implements RelationshipEntry<T, D> {

    private RelationshipRepository relationshipRepository;

    public DirectRelationshipEntry(RelationshipRepository relationshipRepository) {
        this.relationshipRepository = relationshipRepository;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        Class<?>[] typeArgs = TypeResolver
            .resolveRawArguments(RelationshipRepository.class, relationshipRepository.getClass());
        return typeArgs[RelationshipRepository.TARGET_TYPE_GENERIC_PARAMETER_IDX];
    }

    public RelationshipRepository getRelationshipRepository() {
        return relationshipRepository;
    }

    @Override
    public String toString() {
        return "DirectRelationshipEntry{" +
            "relationshipRepository=" + relationshipRepository +
            '}';
    }
}
