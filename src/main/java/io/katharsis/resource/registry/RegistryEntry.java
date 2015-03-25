package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.ResourceInformation;

import java.util.LinkedList;
import java.util.List;

public class RegistryEntry<T> {
    private ResourceInformation<T> resourceInformation;
    private ResourceRepository<T, ?> resourceRepository;
    private List<RelationshipRepository<T, ?, ?, ?>> relationshipRepositories;

    public RegistryEntry(ResourceInformation<T> resourceInformation, ResourceRepository<T, ?> resourceRepository) {
        this(resourceInformation, resourceRepository, new LinkedList<>());
    }

    public RegistryEntry(ResourceInformation<T> resourceInformation, ResourceRepository<T, ?> resourceRepository,
                         List<RelationshipRepository<T, ?, ?, ?>> relationshipRepositories) {
        this.resourceInformation = resourceInformation;
        this.resourceRepository = resourceRepository;
        this.relationshipRepositories = relationshipRepositories;
    }

    public ResourceRepository<T, ?> getResourceRepository() {
        return resourceRepository;
    }

    public List<RelationshipRepository<T, ?, ?, ?>> getRelationshipRepositories() {
        return relationshipRepositories;
    }

    public ResourceInformation<T> getResourceInformation() {
        return resourceInformation;
    }
}
