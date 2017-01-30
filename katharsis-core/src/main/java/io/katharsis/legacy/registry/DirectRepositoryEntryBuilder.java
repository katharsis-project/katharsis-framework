package io.katharsis.legacy.registry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.core.internal.registry.DirectResponseRelationshipEntry;
import io.katharsis.core.internal.registry.DirectResponseResourceEntry;
import io.katharsis.errorhandling.exception.RepositoryInstanceNotFoundException;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.legacy.repository.ResourceRepository;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.registry.ResourceEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResponseRelationshipEntry;
import net.jodah.typetools.TypeResolver;

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
	public ResourceEntry buildResourceRepository(ResourceLookup lookup, Class<?> resourceClass) {
		Class<?> repoClass = getRepoClassType(lookup.getResourceRepositoryClasses(), resourceClass);

		if (repoClass == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		DirectResponseResourceEntry directResourceEntry = new DirectResponseResourceEntry(new RepositoryInstanceBuilder(jsonServiceLocator, repoClass));
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
			if (ResourceRepositoryV2.class.isAssignableFrom(repoClass)) {
				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepositoryV2.class, repoClass);
				if (typeArgs[0] == resourceClass) {
					return repoClass;
				}
			}

		}
		return null;
	}

	@Override
	public List<ResponseRelationshipEntry> buildRelationshipRepositories(ResourceLookup lookup, Class<?> resourceClass) {
		Set<Class<?>> relationshipRepositoryClasses = lookup.getResourceRepositoryClasses();

		Set<Class<?>> relationshipRepositories = findRelationshipRepositories(resourceClass, relationshipRepositoryClasses);

		List<ResponseRelationshipEntry> relationshipEntries = new LinkedList<>();
		for (Class<?> relationshipRepositoryClass : relationshipRepositories) {
			Object relationshipRepository = jsonServiceLocator.getInstance(relationshipRepositoryClass);
			if (relationshipRepository == null) {
				throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
			}

			LOGGER.debug("Assigned {} RelationshipRepository  to {} resource class", relationshipRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

			@SuppressWarnings("unchecked")
			DirectResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(new RepositoryInstanceBuilder<>(jsonServiceLocator, (Class<RelationshipRepository>) relationshipRepositoryClass));
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
			if (RelationshipRepositoryV2.class.isAssignableFrom(repoClass)) {
				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepositoryV2.class, repoClass);
				if (typeArgs[0] == resourceClass) {
					relationshipRepositories.add(repoClass);
				}
			}
		}

		return relationshipRepositories;
	}
}
