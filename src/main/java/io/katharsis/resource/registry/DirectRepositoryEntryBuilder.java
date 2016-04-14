package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.registry.repository.DirectRelationshipEntry;
import io.katharsis.resource.registry.repository.DirectResourceEntry;
import io.katharsis.resource.registry.repository.RelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import net.jodah.typetools.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Repository entries builder for classes implementing repository interfaces.
 */
public class DirectRepositoryEntryBuilder implements RepositoryEntryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectRepositoryEntryBuilder.class);

    private final JsonServiceLocator jsonServiceLocator;

    public DirectRepositoryEntryBuilder(JsonServiceLocator jsonServiceLocator) {
        this.jsonServiceLocator = jsonServiceLocator;
    }

    @Override
    public ResourceEntry<?, ?> buildResourceRepository(ResourceLookup lookup, Class<?> resourceClass) {
        Class<?> repoClass = getRepoClassType(lookup.getResourceRepositoryClasses(), resourceClass);

        if (repoClass == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        DirectResourceEntry directResourceEntry = new DirectResourceEntry(
            new RepositoryInstanceBuilder(jsonServiceLocator, repoClass));
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
    public List<RelationshipEntry<?, ?>> buildRelationshipRepositories(ResourceLookup lookup, Class<?> resourceClass) {
        Set<Class<?>> relationshipRepositoryClasses = lookup.getResourceRepositoryClasses();

        Set<Class<?>> relationshipRepositories =
            findRelationshipRepositories(resourceClass, relationshipRepositoryClasses);

        List<RelationshipEntry<?, ?>> relationshipEntries = new LinkedList<>();
        for (Class<?> relationshipRepositoryClass : relationshipRepositories) {
            RelationshipRepository relationshipRepository = (RelationshipRepository) jsonServiceLocator.getInstance(relationshipRepositoryClass);
            if (relationshipRepository == null) {
                throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
            }

            LOGGER.debug("Assigned {} RelationshipRepository  to {} resource class",
                relationshipRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

            @SuppressWarnings("unchecked")
            DirectRelationshipEntry<Object, Object> relationshipEntry = new DirectRelationshipEntry<>(
                new RepositoryInstanceBuilder<>(jsonServiceLocator, (Class<RelationshipRepository>) relationshipRepositoryClass));
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
