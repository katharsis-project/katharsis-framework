package io.katharsis.resource.registry;

import java.util.Set;

import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.legacy.repository.ResourceRepository;

public interface ResourceLookup {
	
	Set<Class<?>> getResourceClasses();
	
	/**
	 * Returns the repository classes {@link ResourceRepository}, {@link RelationshipRepository}.
	 * 
	 * @return repository classes
	 */
	Set<Class<?>> getResourceRepositoryClasses();
}
