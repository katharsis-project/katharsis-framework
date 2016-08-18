package io.katharsis.module;

import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * Interface for extensions that can be registered to Katharsis to provide a
 * well-defined set of extensions on top of the default functionality.
 */
public interface Module {

	/**
	 * Returns the identifier of this module.
	 */
	String getModuleName();

	/**
	 * Called when the module is registered with Katharsis. Allows the module to
	 * register functionality it provides.
	 */
	void setupModule(ModuleContext context);

	/**
	 * Interface Katharsis exposes to modules for purpose of registering
	 * extended functionality.
	 */
	interface ModuleContext {

		/**
		 * Register the given {@link ResourceInformationBuilder} in Katharsis.
		 * 
		 * @param resourceInformationBuilder
		 */
		void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder);

		/**
		 * Register the given {@link ResourceLookup} in Katharsis.
		 * 
		 * @param resourceLookup
		 */
		void addResourceLookup(ResourceLookup resourceLookup);

		/**
		 * Registers an additional module for Jackson.
		 */
		void addJacksonModule(com.fasterxml.jackson.databind.Module module);

		/**
		 * Adds the given repository for the given type.
		 */
		void addRepository(Class<?> resourceClass, ResourceRepository<?, ?> repository);

		/**
		 * Adds the given repository for the given source and target type.
		 */
		void addRepository(Class<?> sourceResourceClass, Class<?> targetResourceClass, RelationshipRepository<?, ?, ?, ?> repository);

		/**
		 * Adds a new exception mapper lookup.
		 */
		void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup);
		
		/**
		 * Adds a new exception mapper lookup.
		 */
		void addExceptionMapper(ExceptionMapper<?> exceptionMapper);
		
		/**
		 * Adds a filter to intercept requests.
		 */
		void addFilter(Filter filter);
		
		/**
		 * Returns the ResourceRegistry. Note that instance is not yet available
		 * when {@link Module#setupModule(ModuleContext)} is called. So
		 * consumers may have to hold onto the {@link ModuleContext} instead.
		 * 
		 * @return ResourceRegistry
		 */
		ResourceRegistry getResourceRegistry();

	}
}
