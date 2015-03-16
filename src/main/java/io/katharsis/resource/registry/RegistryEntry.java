package io.katharsis.resource.registry;

import io.katharsis.repository.EntityRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.ResourceInformation;

import java.util.LinkedList;
import java.util.List;

public class RegistryEntry<T> {
    private ResourceInformation<T> resourceInformation;
    private EntityRepository<T, ?> entityRepository;
    private List<RelationshipRepository<T, ?>> relationshipRepositories;

    public RegistryEntry(ResourceInformation<T> resourceInformation, EntityRepository<T, ?> entityRepository) {
        this(resourceInformation, entityRepository, new LinkedList<>());
    }

    public RegistryEntry(ResourceInformation<T> resourceInformation, EntityRepository<T, ?> entityRepository,
                         List<RelationshipRepository<T, ?>> relationshipRepositories) {
        this.resourceInformation = resourceInformation;
        this.entityRepository = entityRepository;
        this.relationshipRepositories = relationshipRepositories;
    }

    public EntityRepository<T, ?> getEntityRepository() {
        return entityRepository;
    }

    public List<RelationshipRepository<T, ?>> getRelationshipRepositories() {
        return relationshipRepositories;
    }

    public ResourceInformation<T> getResourceInformation() {
        return resourceInformation;
    }
}
