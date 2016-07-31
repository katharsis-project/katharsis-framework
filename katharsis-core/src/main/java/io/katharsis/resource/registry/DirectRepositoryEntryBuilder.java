package io.katharsis.resource.registry;

import io.katharsis.locator.RepositoryFactory;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.registry.repository.DirectResponseRelationshipEntry;
import io.katharsis.resource.registry.repository.DirectResponseResourceEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import io.katharsis.resource.registry.repository.ResponseRelationshipEntry;
import lombok.extern.slf4j.Slf4j;
import net.jodah.typetools.TypeResolver;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Repository entries builder for classes implementing repository interfaces.
 */
@Slf4j
public class DirectRepositoryEntryBuilder implements RepositoryEntryBuilder {

    private final RepositoryFactory repositoryFactory;

    public DirectRepositoryEntryBuilder(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    @Override
    public ResourceEntry<?, ?> buildResourceRepository(ResourceLookup lookup, Class<?> resourceClass) {
        Class<?> repoClass = getRepoClassType(lookup.getResourceRepositoryClasses(), resourceClass);

        if (repoClass == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        DirectResponseResourceEntry directResourceEntry = new DirectResponseResourceEntry(
                new RepositoryInstanceBuilder(repositoryFactory, repoClass));
        return directResourceEntry;
    }

    private Class<?> getRepoClassType(Set<Class<?>> repositoryClasses, Class<?> resourceClass) {
        for (Class<?> repoClass : repositoryClasses) {
            if (ResourceRepository.class.isAssignableFrom(repoClass)) {
                Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, repoClass);
                if (typeArgs[0] == resourceClass) {
                    return repoClass;
                }
            }
        }
        return null;
    }

    @Override
    public List<ResponseRelationshipEntry<?, ?>> buildRelationshipRepositories(ResourceLookup lookup, Class<?> resourceClass) {
        Set<Class<?>> relationshipRepositoryClasses = lookup.getResourceRepositoryClasses();

        Set<Class<?>> relationshipRepositories =
                findRelationshipRepositories(resourceClass, relationshipRepositoryClasses);

        List<ResponseRelationshipEntry<?, ?>> relationshipEntries = new LinkedList<>();
        for (Class<?> relationshipRepositoryClass : relationshipRepositories) {
            RelationshipRepository relationshipRepository = (RelationshipRepository) repositoryFactory.getInstance(relationshipRepositoryClass);
            if (relationshipRepository == null) {
                throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
            }

            log.debug("Assigned {} RelationshipRepository  to {} resource class",
                    relationshipRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

            @SuppressWarnings("unchecked")
            DirectResponseRelationshipEntry<Object, Object> relationshipEntry = new DirectResponseRelationshipEntry<>(
                    new RepositoryInstanceBuilder<>(repositoryFactory, (Class<RelationshipRepository>) relationshipRepositoryClass));
            relationshipEntries.add(relationshipEntry);
        }
        return relationshipEntries;
    }

    private Set<Class<?>> findRelationshipRepositories(Class resourceClass, Set<Class<?>> relationshipRepositoryClasses) {
        Set<Class<?>> relationshipRepositories = new HashSet<>();
        for (Class<?> repoClass : relationshipRepositoryClasses) {
            if (RelationshipRepository.class.isAssignableFrom(repoClass)) {
                Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repoClass);
                if (typeArgs[0] == resourceClass) {
                    relationshipRepositories.add(repoClass);
                }
            }
        }

        return relationshipRepositories;
    }
}
