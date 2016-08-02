package io.katharsis.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.module.SimpleModule.RelationshipRepositoryRegistration;
import io.katharsis.module.SimpleModule.ResourceRepositoryRegistration;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.DirectResponseRelationshipEntry;
import io.katharsis.resource.registry.repository.DirectResponseResourceEntry;
import io.katharsis.resource.registry.repository.ResponseRelationshipEntry;

/**
 * Container for setting up and holding {@link Module} instances;
 */
public class ModuleRegistry {

	private ObjectMapper objectMapper;
	private ResourceRegistry resourceRegistry;

	private List<Module> modules = new ArrayList<Module>();

	private SimpleModule aggregatedModule = new SimpleModule(null);
	private boolean initalized;

	public ModuleRegistry() {
	}

	/**
	 * Register an new module to this registry and setup the module.
	 * 
	 * @param module
	 */
	public void addModule(Module module) {
		module.setupModule(new ModuleContextImpl());
		modules.add(module);
	}

	class ModuleContextImpl implements Module.ModuleContext {

		@Override
		public void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder) {
			checkNotInitialized();
			aggregatedModule.addResourceInformationBuilder(resourceInformationBuilder);
		}

		@Override
		public void addResourceLookup(ResourceLookup resourceLookup) {
			checkNotInitialized();
			aggregatedModule.addResourceLookup(resourceLookup);
		}

		@Override
		public void addJacksonModule(com.fasterxml.jackson.databind.Module module) {
			checkNotInitialized();
			aggregatedModule.addJacksonModule(module);
			if (objectMapper != null) {
				objectMapper.registerModule(module);
			}
		}

		@Override
		public ResourceRegistry getResourceRegistry() {
			if (resourceRegistry == null)
				throw new IllegalStateException("resourceRegistry not yet available");
			return resourceRegistry;
		}

		@Override
		public void addRepository(Class<?> type, ResourceRepository<?, ?> repository) {
			checkNotInitialized();
			aggregatedModule.addRepository(type, repository);
		}

		@Override
		public void addRepository(Class<?> sourceType, Class<?> targetType,
				RelationshipRepository<?, ?, ?, ?> repository) {
			checkNotInitialized();
			aggregatedModule.addRepository(sourceType, targetType, repository);
		}

		@Override
		public void addFilter(Filter filter) {
			aggregatedModule.addFilter(filter);			
		}
	}

	/**
	 * Returns all Jackson modules registered by modules.
	 */
	public List<com.fasterxml.jackson.databind.Module> getJacksonModules() {
		return aggregatedModule.getJacksonModules();
	}

	/**
	 * Ensures the {@link ModuleRegistry#init(ObjectMapper, ResourceRegistry)} has not yet been called.
	 */
	protected void checkNotInitialized() {
		if (initalized) {
			throw new IllegalStateException("already initialized, cannot be changed anymore");
		}
	}

	/**
	 * Returns a {@link ResourceInformationBuilder} instance that combines all 
	 * instances registered by modules.
	 */
	public ResourceInformationBuilder getResourceInformationBuilder() {
		return new CombinedResourceInformationBuilder(aggregatedModule.getResourceInformationBuilders());
	}

	/**
	 * Returns a {@link ResourceLookup} instance that combines all 
	 * instances registered by modules.
	 */
	public ResourceLookup getResourceLookup() {
		return new CombinedResourceLookup(aggregatedModule.getResourceLookups());
	}

	/**
	 * Combines all {@link ResourceInformationBuilder} instances provided by the registered
	 * {@link Module}.
	 */
	static class CombinedResourceInformationBuilder implements ResourceInformationBuilder {

		private Collection<ResourceInformationBuilder> builders;

		public CombinedResourceInformationBuilder(List<ResourceInformationBuilder> builders) {
			this.builders = builders;
		}

		@Override
		public boolean accept(Class<?> resourceClass) {
			for (ResourceInformationBuilder builder : builders) {
				if (builder.accept(resourceClass)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public ResourceInformation build(Class<?> resourceClass) {
			for (ResourceInformationBuilder builder : builders) {
				if (builder.accept(resourceClass)) {
					return builder.build(resourceClass);
				}
			}
			throw new UnsupportedOperationException(
					"no ResourceInformationBuilder for " + resourceClass.getName() + " available");
		}

	}

	/**
	 * Combines all {@link ResourceLookup} instances provided by the registered
	 * {@link Module}.
	 */
	static class CombinedResourceLookup implements ResourceLookup {

		private Collection<ResourceLookup> lookups;

		public CombinedResourceLookup(List<ResourceLookup> lookups) {
			this.lookups = lookups;
		}

		@Override
		public Set<Class<?>> getResourceClasses() {
			Set<Class<?>> set = new HashSet<Class<?>>();
			for (ResourceLookup lookup : lookups) {
				set.addAll(lookup.getResourceClasses());
			}
			return set;
		}

		@Override
		public Set<Class<?>> getResourceRepositoryClasses() {
			Set<Class<?>> set = new HashSet<Class<?>>();
			for (ResourceLookup lookup : lookups) {
				set.addAll(lookup.getResourceRepositoryClasses());
			}
			return set;
		}
	}

	/**
	 * Initializes the {@link ModuleRegistry} and applies all pending changes. After the initialization
	 * completed, it is not possible to add any further modules.
	 * 
	 * @param objectMapper
	 * @param resourceRegistry
	 */
	public void init(ObjectMapper objectMapper, ResourceRegistry resourceRegistry) {
		this.initalized = true;
		this.objectMapper = objectMapper;
		this.resourceRegistry = resourceRegistry;
		this.objectMapper.registerModules(getJacksonModules());

		applyRepositoryRegistration(resourceRegistry);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void applyRepositoryRegistration(ResourceRegistry resourceRegistry) {
		List<RelationshipRepositoryRegistration> relationshipRepositoryRegistrations = aggregatedModule
				.getRelationshipRepositoryRegistrations();
		List<ResourceRepositoryRegistration> resourceRepositoryRegistrations = aggregatedModule
				.getResourceRepositoryRegistrations();

		// TODO this needs to be merged with ResourceRegistryBuilder
		for (final ResourceRepositoryRegistration resourceRepositoryRegistration : resourceRepositoryRegistrations) {
			Class<?> resourceClass = resourceRepositoryRegistration.getResourceClass();
			RepositoryInstanceBuilder<ResourceRepository<?, ?>> repositoryInstanceBuilder = new RepositoryInstanceBuilder(
					null, null) {
				public Object buildRepository() {
					return resourceRepositoryRegistration.getRepository();
				}
			};
			DirectResponseResourceEntry resourceEntry = new DirectResponseResourceEntry(repositoryInstanceBuilder);
			ResourceInformation resourceInformation = getResourceInformationBuilder().build(resourceClass);
			List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<ResponseRelationshipEntry>();
			for (final RelationshipRepositoryRegistration relationshipRepositoryRegistration : relationshipRepositoryRegistrations) {
				if (relationshipRepositoryRegistration.getSourceType() == resourceClass) {
					RepositoryInstanceBuilder<RelationshipRepository> relationshipInstanceBuilder = new RepositoryInstanceBuilder<RelationshipRepository>(
							null, null) {
						public RelationshipRepository buildRepository() {
							return relationshipRepositoryRegistration.getRepository();
						}
					};
					ResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(
							relationshipInstanceBuilder);
					relationshipEntries.add(relationshipEntry);
				}
			}
			// TODO get also relations from resource lookup
			RegistryEntry registryEntry = new RegistryEntry(resourceInformation, resourceEntry, relationshipEntries);
			resourceRegistry.addEntry(resourceClass, registryEntry);
		}
	}

	/**
	 * @return filters added by all modules
	 */
	public List<Filter> getFilters() {
		return aggregatedModule.getFilters();
	}
	
}
