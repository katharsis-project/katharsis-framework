package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.repository.DirectResourceRepositoryEntry;
import io.katharsis.resource.registry.repository.ParametrizedResourceRepositoryEntry;
import io.katharsis.resource.registry.repository.ResourceRepositoryEntry;
import net.jodah.typetools.TypeResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Holds information about a resource of type <i>T</i> and its repositories.
 * It includes the following information:
 * - ResourceInformation instance with information about the resource,
 * - ResourceRepositoryEntry instance,
 * - List of all repositories for relationships defined in resource class.
 * - Parent RegistryEntry if a resource inherits from another resource
 *
 * @param <T> resource type
 */
public class RegistryEntry<T> {
    private final ResourceInformation resourceInformation;
    private final ResourceRepositoryEntry<T, ?> resourceRepositoryEntry;
    private final List<RelationshipRepository<T, ?, ?, ?>> relationshipRepositories;
    private RegistryEntry parentRegistryEntry = null;

    public RegistryEntry(ResourceInformation resourceInformation,
                         @SuppressWarnings("SameParameterValue") ResourceRepositoryEntry<T, ?> resourceRepositoryEntry) {
        this(resourceInformation, resourceRepositoryEntry, new LinkedList<>());
    }

    public RegistryEntry(ResourceInformation resourceInformation,
                         ResourceRepositoryEntry<T, ?> resourceRepositoryEntry,
                         List<RelationshipRepository<T, ?, ?, ?>> relationshipRepositories) {
        this.resourceInformation = resourceInformation;
        this.resourceRepositoryEntry = resourceRepositoryEntry;
        this.relationshipRepositories = relationshipRepositories;
    }

    public ResourceRepository<T, ?> getResourceRepository(ResourceMethodParameterProvider parameterProvider) {
        ResourceRepository<T, ?> repo = null;
        if (resourceRepositoryEntry instanceof DirectResourceRepositoryEntry) {
            repo = ((DirectResourceRepositoryEntry<T, ?>) resourceRepositoryEntry).getResourceRepository();
        } else if (resourceRepositoryEntry instanceof ParametrizedResourceRepositoryEntry) {
            repo = ((ParametrizedResourceRepositoryEntry<T, ?>) resourceRepositoryEntry).buildResourceRepository(parameterProvider);
        }
        return repo;
    }

    public List<RelationshipRepository<T, ?, ?, ?>> getRelationshipRepositories() {
        return relationshipRepositories;
    }

    public RelationshipRepository<T, ?, ?, ?> getRelationshipRepositoryForClass(Class clazz) {
        RelationshipRepository<T, ?, ?, ?> foundRelationshipRepository = null;
        for (RelationshipRepository<T, ?, ?, ?> relationshipRepository : relationshipRepositories) {
            Class<?>[] typeArgs = TypeResolver
                .resolveRawArguments(RelationshipRepository.class, relationshipRepository.getClass());

            if (clazz == typeArgs[RelationshipRepository.TARGET_TYPE_GENERIC_PARAMETER_IDX]) {
                foundRelationshipRepository = relationshipRepository;
            }
        }
        if (foundRelationshipRepository == null) {
            throw new RelationshipRepositoryNotFoundException(resourceInformation.getResourceClass(), clazz);
        }

        return foundRelationshipRepository;
    }

    public ResourceInformation getResourceInformation() {
        return resourceInformation;
    }

    public RegistryEntry getParentRegistryEntry() {
        return parentRegistryEntry;
    }

    /**
     * To be used only by ResourceRegistryBuilder
     *
     * @param parentRegistryEntry parent resource
     */
    void setParentRegistryEntry(RegistryEntry parentRegistryEntry) {
        this.parentRegistryEntry = parentRegistryEntry;
    }

    /**
     * Check the parameter is a parent of <b>this</b> {@link RegistryEntry} instance
     *
     * @param registryEntry parent to check
     * @return true if the parameter is a parent
     */
    public boolean isParent(RegistryEntry registryEntry) {
        RegistryEntry parentRegistryEntry = getParentRegistryEntry();
        while (parentRegistryEntry != null) {
            if (parentRegistryEntry.equals(registryEntry)) {
                return true;
            }
            parentRegistryEntry = parentRegistryEntry.getParentRegistryEntry();
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryEntry<?> that = (RegistryEntry<?>) o;
        return Objects.equals(resourceInformation, that.resourceInformation) &&
            Objects.equals(resourceRepositoryEntry, that.resourceRepositoryEntry) &&
            Objects.equals(relationshipRepositories, that.relationshipRepositories) &&
            Objects.equals(parentRegistryEntry, that.parentRegistryEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceInformation, resourceRepositoryEntry, relationshipRepositories, parentRegistryEntry);
    }
}
