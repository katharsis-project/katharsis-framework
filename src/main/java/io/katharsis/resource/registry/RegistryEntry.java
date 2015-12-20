package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.repository.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Holds information about a resource of type <i>T</i> and its repositories.
 * It includes the following information:
 * - ResourceInformation instance with information about the resource,
 * - ResourceEntry instance,
 * - List of all repositories for relationships defined in resource class.
 * - Parent RegistryEntry if a resource inherits from another resource
 *
 * @param <T> resource type
 */
public class RegistryEntry<T> {
    private final ResourceInformation resourceInformation;
    private final ResourceEntry<T, ?> resourceEntry;
    private final List<RelationshipEntry<T, ?>> relationshipEntries;
    private RegistryEntry parentRegistryEntry = null;

    public RegistryEntry(ResourceInformation resourceInformation,
                         @SuppressWarnings("SameParameterValue") ResourceEntry<T, ?> resourceEntry) {
        this(resourceInformation, resourceEntry, new LinkedList<RelationshipEntry<T, ?>>());
    }

    public RegistryEntry(ResourceInformation resourceInformation,
                         ResourceEntry<T, ?> resourceEntry,
                         List<RelationshipEntry<T, ?>> relationshipEntries) {
        this.resourceInformation = resourceInformation;
        this.resourceEntry = resourceEntry;
        this.relationshipEntries = relationshipEntries;
    }

    public ResourceRepository<T, ?> getResourceRepository(RepositoryMethodParameterProvider parameterProvider) {
        ResourceRepository<T, ?> repoInstance = null;
        if (resourceEntry instanceof DirectResourceEntry) {
            repoInstance = ((DirectResourceEntry<T, ?>) resourceEntry).getResourceRepository();
        } else if (resourceEntry instanceof AnnotatedResourceEntryBuilder) {
            repoInstance = ((AnnotatedResourceEntryBuilder<T, ?>) resourceEntry).build(parameterProvider);
        }
        return repoInstance;
    }

    public List<RelationshipEntry<T, ?>> getRelationshipEntries() {
        return relationshipEntries;
    }

    public RelationshipRepository<T, ?, ?, ?> getRelationshipRepositoryForClass(Class clazz, RepositoryMethodParameterProvider parameterProvider) {
        RelationshipEntry<T, ?> foundRelationshipEntry = null;
        for (RelationshipEntry<T, ?> relationshipEntry : relationshipEntries) {
            if (clazz == relationshipEntry.getTargetAffiliation()) {
                foundRelationshipEntry = relationshipEntry;
                break;
            }
        }
        if (foundRelationshipEntry == null) {
            throw new RelationshipRepositoryNotFoundException(resourceInformation.getResourceClass(), clazz);
        }

        RelationshipRepository<T, ?, ?, ?> repoInstance = null;
        if (foundRelationshipEntry instanceof DirectRelationshipEntry) {
            repoInstance = ((DirectRelationshipEntry<T, ?>) foundRelationshipEntry).getRelationshipRepository();
        } else if (foundRelationshipEntry instanceof AnnotatedRelationshipEntryBuilder) {
            repoInstance = ((AnnotatedRelationshipEntryBuilder<T, ?>) foundRelationshipEntry).build(parameterProvider);
        }

        return repoInstance;
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
            Objects.equals(resourceEntry, that.resourceEntry) &&
            Objects.equals(relationshipEntries, that.relationshipEntries) &&
            Objects.equals(parentRegistryEntry, that.parentRegistryEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceInformation, resourceEntry, relationshipEntries, parentRegistryEntry);
    }
}
