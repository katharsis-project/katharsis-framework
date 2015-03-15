package io.katharsis.resource.registry;

import io.katharsis.repository.EntityRepository;
import io.katharsis.repository.RelationshipRepository;

import java.util.LinkedList;
import java.util.List;

public class RegistryEntry<T> {
    private EntityRepository<T, ?> entityRepository;
    private List<RelationshipRepository<T, ?>> relationshipRepositories;

    public RegistryEntry(EntityRepository<T, ?> entityRepository) {
        this(entityRepository, new LinkedList<>());
    }

    public RegistryEntry(EntityRepository<T, ?> entityRepository, List<RelationshipRepository<T, ?>> relationshipRepositories) {
        this.entityRepository = entityRepository;
        this.relationshipRepositories = relationshipRepositories;
    }

    public EntityRepository<T, ?> getEntityRepository() {
        return entityRepository;
    }

    public List<RelationshipRepository<T, ?>> getRelationshipRepositories() {
        return relationshipRepositories;
    }
}
