package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.NotFoundRepository;
import io.katharsis.resource.registry.repository.DirectResourceEntry;
import io.katharsis.resource.registry.repository.RelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains a strategy to decide which implementation of an entry will be provided. Keep in mind that there can be a
 * case in which there will be two repositories of the same types.
 */
public class RepositoryEntryBuilderFacade implements RepositoryEntryBuilder {

    private final DirectRepositoryEntryBuilder directRepositoryEntryBuilder;
    private final AnnotatedRepositoryEntryBuilder annotatedRepositoryEntryBuilder;

    public RepositoryEntryBuilderFacade(JsonServiceLocator jsonServiceLocator) {
        this.directRepositoryEntryBuilder = new DirectRepositoryEntryBuilder(jsonServiceLocator);
        this.annotatedRepositoryEntryBuilder = new AnnotatedRepositoryEntryBuilder(jsonServiceLocator);
    }

    @Override
    public ResourceEntry<?, ?> buildResourceRepository(ResourceLookup lookup, Class<?> resourceClass) {
    	ResourceEntry<?, ?> resourceEntry =  annotatedRepositoryEntryBuilder.buildResourceRepository(lookup, resourceClass);
        if (resourceEntry == null) {
            resourceEntry = directRepositoryEntryBuilder.buildResourceRepository(lookup, resourceClass);
        }
        if (resourceEntry == null) {
            resourceEntry = new DirectResourceEntry<>(new NotFoundRepository<>(resourceClass));
        }

        return resourceEntry;
    }

    @Override
    public List<RelationshipEntry<?, ?>> buildRelationshipRepositories(ResourceLookup lookup, Class<?> resourceClass) {
        List<RelationshipEntry<?, ?>> annotationEntries = annotatedRepositoryEntryBuilder
            .buildRelationshipRepositories(lookup, resourceClass);
        List<RelationshipEntry<?, ?>> targetEntries = new LinkedList<>(annotationEntries);
        List<RelationshipEntry<?, ?>> directEntries = directRepositoryEntryBuilder
            .buildRelationshipRepositories(lookup, resourceClass);

        directEntries.forEach(
            directEntry -> {
                if (!contains(targetEntries, directEntry)) {
                    targetEntries.add(directEntry);
                }
            }
        );

        return targetEntries;
    }

    private boolean contains(List<RelationshipEntry<?, ?>> targetEntries, RelationshipEntry<?, ?> directEntry) {
        boolean contains = false;
        for (RelationshipEntry<?, ?> targetEntry : targetEntries) {
            if (targetEntry.getTargetAffiliation().equals(directEntry.getTargetAffiliation())) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
