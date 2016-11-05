package io.katharsis.module;

import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.repository.filter.RepositoryFilter;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.security.SecurityProvider;

/**
 * Interface for extensions that can be registered to Katharsis to provide a
 * well-defined set of extensions on top of the default functionality.
 */
public interface Module {

	/**
	 * Returns the identifier of this module.
	 *
	 * @return module name
	 */
	String getModuleName();

	/**
	 * Called when the module is registered with Katharsis. Allows the module to
	 * register functionality it provides.
	 *
	 * @param context context
	 */
	void setupModule(ModuleContext context);

	/**
	 * Interface Katharsis exposes to modules for purpose of registering
	 * extended functionality.
	 */
	interface ModuleContext {

		/**
		 * @return ServiceDiscovery
		 */
		public ServiceDiscovery getServiceDiscovery();

		/**
		 * Register the given {@link ResourceInformationBuilder} in Katharsis.
		 *
		 * @param resourceInformationBuilder resource information builder
		 */
		void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder);

		/**
		 * Register the given {@link ResourceLookup} in Katharsis.
		 *
		 * @param resourceLookup resource lookup
		 */
		void addResourceLookup(ResourceLookup resourceLookup);

		/**
		 * Registers an additional module for Jackson.
		 *
		 * @param module module
		 */
		void addJacksonModule(com.fasterxml.jackson.databind.Module module);

		/**
		 * Adds the given repository for the given type.
		 *
		 * @param resourceClass resource class
		 * @param repository    repository
		 */
		void addRepository(Class<?> resourceClass, Object repository);

		/**
		 * Adds the given repository for the given source and target type.
		 *
		 * @param sourceResourceClass source resource class
		 * @param targetResourceClass target resource class
		 * @param repository          repository
		 */
		void addRepository(Class<?> sourceResourceClass, Class<?> targetResourceClass, Object repository);

		/**
		 * Adds a new exception mapper lookup.
		 *
		 * @param exceptionMapperLookup exception mapper lookup
		 */
		void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup);

		/**
		 * Adds a new exception mapper lookup.
		 *
		 * @param exceptionMapper exception mapper
		 */
		void addExceptionMapper(ExceptionMapper<?> exceptionMapper);

		/**
		 * Adds a filter to intercept requests.
		 *
		 * @param filter filter
		 */
		void addFilter(Filter filter);
		
		/**
		 * Adds a repository filter to intercept repository calls.
		 *
		 * @param RepositoryFilter filter
		 */
		void addRepositoryFilter(RepositoryFilter filter);

		/**
		 * Returns the ResourceRegistry. Note that instance is not yet available
		 * when {@link Module#setupModule(ModuleContext)} is called. So
		 * consumers may have to hold onto the {@link ModuleContext} instead.
		 *
		 * @return ResourceRegistry
		 */
		ResourceRegistry getResourceRegistry();

		/**
		 * Adds a securityProvider.
		 * 
		 * @param securityProvider Ask remo
		 */
		void addSecurityProvider(SecurityProvider securityProvider);

		/**
		 * Returns the security provider. Provides access to security related feature independent of the underlying implementation.
		 */
		public SecurityProvider getSecurityProvider();

	}
}
