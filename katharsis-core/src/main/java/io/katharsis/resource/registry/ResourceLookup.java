package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;

import java.util.Set;

public interface ResourceLookup {
	
	Set<Class<?>> getResourceClasses();
	
	/**
	 * Returns the repository classes {@link ResourceRepository}, {@link RelationshipRepository}.
	 * 
	 * @return repository classes
	 */
	Set<Class<?>> getResourceRepositoryClasses();
}
