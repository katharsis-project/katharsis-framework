package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.ResourceInformation;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Holds information about a resource of type <i>T</i> and its repositories.
 * It includes the following information:
 * - ResourceInformation instance with information about the resource,
 * - ResourceRepository instance,
 * - List of all repositories for relationships defined in resource class.
 *
 * @param <T> resource type
 */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryEntry<?> that = (RegistryEntry<?>) o;
        return Objects.equals(resourceInformation, that.resourceInformation) &&
                Objects.equals(resourceRepository, that.resourceRepository) &&
                Objects.equals(relationshipRepositories, that.relationshipRepositories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceInformation, resourceRepository, relationshipRepositories);
    }
}
