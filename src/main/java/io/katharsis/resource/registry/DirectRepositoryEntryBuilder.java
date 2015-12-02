package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.registry.repository.DirectRelationshipEntry;
import io.katharsis.resource.registry.repository.DirectResourceEntry;
import io.katharsis.resource.registry.repository.RelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.jodah.typetools.TypeResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Optional<Class<?>> repoClass = lookup.getResourceRepositoryClasses()
            .stream()
            .filter(ResourceRepository.class::isAssignableFrom)
            .filter(clazz -> {
                Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, clazz);
                return typeArgs[0] == resourceClass;
            })
            .findFirst();
        if (!repoClass.isPresent()) {
            return null;
        }
        ResourceRepository<?, ?> repoInstance = (ResourceRepository<?, ?>) jsonServiceLocator.getInstance(repoClass.get());
        if (repoInstance == null) {
            throw new RepositoryInstanceNotFoundException(repoClass.get().getCanonicalName());
        }
        return new DirectResourceEntry<>(repoInstance);
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

            relationshipEntries.add(new DirectRelationshipEntry<>(relationshipRepository));
        }
        return relationshipEntries;
    }

    private Set<Class<?>> findRelationshipRepositories(Class resourceClass, Set<Class<?>> relationshipRepositoryClasses) {
    	return relationshipRepositoryClasses.stream()
    		.filter(RelationshipRepository.class::isAssignableFrom)
    		.filter(clazz-> {
                Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, clazz);
                return typeArgs[0] == resourceClass;
    		}).collect(Collectors.toSet());
    }
}
