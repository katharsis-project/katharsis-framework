package io.katharsis.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.module.SimpleModule.RelationshipRepositoryRegistration;
import io.katharsis.module.SimpleModule.ResourceRepositoryRegistration;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.filter.RepositoryFilter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.MultiResourceLookup;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.AnnotatedRelationshipEntryBuilder;
import io.katharsis.resource.registry.repository.AnnotatedResourceEntry;
import io.katharsis.resource.registry.repository.DirectResponseRelationshipEntry;
import io.katharsis.resource.registry.repository.DirectResponseResourceEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import io.katharsis.resource.registry.repository.ResponseRelationshipEntry;
import io.katharsis.security.SecurityProvider;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PreconditionUtil;

/**
 * Container for setting up and holding {@link Module} instances;
 */
public class ModuleRegistry {

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private List<Module> modules = new ArrayList<>();

	private SimpleModule aggregatedModule = new SimpleModule(null);

	private volatile boolean initialized;

	private ServiceDiscovery serviceDiscovery;

	/**
	 * Register an new module to this registry and setup the module.
	 * 
	 * @param module module
	 */
	public void addModule(Module module) {
		module.setupModule(new ModuleContextImpl());
		modules.add(module);
	}

	public ResourceRegistry getResourceRegistry() {
		if (resourceRegistry == null)
			throw new IllegalStateException("resourceRegistry not yet available");
		return resourceRegistry;
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
		public void addFilter(Filter filter) {
			checkNotInitialized();
			aggregatedModule.addFilter(filter);
		}

		@Override
		public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
			checkNotInitialized();
			aggregatedModule.addExceptionMapperLookup(exceptionMapperLookup);
		}

		@Override
		public void addExceptionMapper(ExceptionMapper<?> exceptionMapper) {
			checkNotInitialized();
			aggregatedModule.addExceptionMapper(exceptionMapper);
		}

		@Override
		public void addRepository(Class<?> type, Object repository) {
			checkNotInitialized();
			aggregatedModule.addRepository(type, repository);
		}

		@Override
		public void addRepository(Class<?> sourceType, Class<?> targetType, Object repository) {
			checkNotInitialized();
			aggregatedModule.addRepository(sourceType, targetType, repository);
		}

		@Override
		public void addSecurityProvider(SecurityProvider securityProvider) {
			checkNotInitialized();
			aggregatedModule.addSecurityProvider(securityProvider);
		}

		@Override
		public SecurityProvider getSecurityProvider() {
			return ModuleRegistry.this.getSecurityProvider();
		}

		@Override
		public ServiceDiscovery getServiceDiscovery() {
			return ModuleRegistry.this.getServiceDiscovery();
		}

		@Override
		public void addRepositoryFilter(RepositoryFilter filter) {
			checkNotInitialized();
			aggregatedModule.addRepositoryFilter(filter);			
		}
	}

	/**
	 *
	 * @return all Jackson modules registered by modules.
	 */
	public List<com.fasterxml.jackson.databind.Module> getJacksonModules() {
		return aggregatedModule.getJacksonModules();
	}

	/**
	 * Ensures the {@link ModuleRegistry#init(ObjectMapper, ResourceRegistry)} has not yet been called.
	 */
	protected void checkNotInitialized() {
		if (initialized) {
			throw new IllegalStateException("already initialized, cannot be changed anymore");
		}
	}

	/**
	 * Returns a {@link ResourceInformationBuilder} instance that combines all
	 * instances registered by modules.
	 *
	 * @return resource information builder
	 */
	public ResourceInformationBuilder getResourceInformationBuilder() {
		return new CombinedResourceInformationBuilder(aggregatedModule.getResourceInformationBuilders());
	}

	/**
	 * Returns a {@link ResourceLookup} instance that combines all
	 * instances registered by modules.
	 *
	 * @return resource lookup
	 */
	public ResourceLookup getResourceLookup() {
		return new MultiResourceLookup(aggregatedModule.getResourceLookups());
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all
	 * instances registered by modules.
	 *
	 * @return resource lookup
	 */
	public SecurityProvider getSecurityProvider() {
		List<SecurityProvider> securityProviders = aggregatedModule.getSecurityProviders();
		PreconditionUtil.assertEquals("exactly one security provide must be installed, got: " + securityProviders, 1,
				securityProviders.size());
		return securityProviders.get(0);
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all
	 * instances registered by modules.
	 *
	 * @return resource lookup
	 */
	public ServiceDiscovery getServiceDiscovery() {
		PreconditionUtil.assertNotNull("serviceDiscovery not yet available", serviceDiscovery);
		return serviceDiscovery;
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
	 * Combines all {@link ExceptionMapperLookup} instances provided by the registered
	 * {@link Module}.
	 */
	static class CombinedExceptionMapperLookup implements ExceptionMapperLookup {

		private Collection<ExceptionMapperLookup> lookups;

		public CombinedExceptionMapperLookup(List<ExceptionMapperLookup> lookups) {
			this.lookups = lookups;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			Set<JsonApiExceptionMapper> set = new HashSet<JsonApiExceptionMapper>();
			for (ExceptionMapperLookup lookup : lookups) {
				set.addAll(lookup.getExceptionMappers());
			}
			return set;
		}
	}

	/**
	 * Initializes the {@link ModuleRegistry} and applies all pending changes. After the initialization
	 * completed, it is not possible to add any further modules.
	 * 
	 * @param objectMapper object mapper
	 * @param resourceRegistry resource registry
	 */
	public void init(ObjectMapper objectMapper) {
		if (!initialized) {
			this.initialized = true;
			this.objectMapper = objectMapper;
			this.objectMapper.registerModules(getJacksonModules());

			applyRepositoryRegistration(resourceRegistry);

			for (Module module : modules) {
				if (module instanceof InitialzingModule) {
					((InitialzingModule) module).init();
				}
			}
		}
	}

	public void setServiceDiscovery(ServiceDiscovery serviceDiscovery){
		this.serviceDiscovery = serviceDiscovery;
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
			final Object repository = resourceRepositoryRegistration.getRepository();
			RepositoryInstanceBuilder<ResourceRepository<?, ?>> repositoryInstanceBuilder = new RepositoryInstanceBuilder(null,
					null) {

				@Override
				public Object buildRepository() {
					return repository;
				}
			};

			ResourceEntry resourceEntry;
			if (ClassUtils.getAnnotation(repository.getClass(), JsonApiResourceRepository.class).isPresent()) {
				resourceEntry = new AnnotatedResourceEntry(repositoryInstanceBuilder);
			}
			else {
				resourceEntry = new DirectResponseResourceEntry(repositoryInstanceBuilder);
			}

			ResourceInformation resourceInformation = getResourceInformationBuilder().build(resourceClass);
			List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>();
			for (final RelationshipRepositoryRegistration relationshipRepositoryRegistration : relationshipRepositoryRegistrations) {
				if (relationshipRepositoryRegistration.getSourceType() == resourceClass) {
					setupRelationShip(relationshipEntries, relationshipRepositoryRegistration);
				}
			}
			// TODO get also relations from resource lookup
			RegistryEntry registryEntry = new RegistryEntry(resourceInformation, resourceEntry, relationshipEntries);
			resourceRegistry.addEntry(resourceClass, registryEntry);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupRelationShip(List<ResponseRelationshipEntry> relationshipEntries,
			final RelationshipRepositoryRegistration relationshipRepositoryRegistration) {
		Object relRepository = relationshipRepositoryRegistration.getRepository();
		RepositoryInstanceBuilder<Object> relationshipInstanceBuilder = new RepositoryInstanceBuilder<Object>(null, null) {

			@Override
			public Object buildRepository() {
				return relationshipRepositoryRegistration.getRepository();
			}

			@Override
			public Class getRepositoryClass() {
				return relationshipRepositoryRegistration.getRepository().getClass();
			}
		};

		if (ClassUtils.getAnnotation(relRepository.getClass(), JsonApiRelationshipRepository.class).isPresent()) {
			relationshipEntries.add(new AnnotatedRelationshipEntryBuilder(relationshipInstanceBuilder));
		}
		else {
			ResponseRelationshipEntry<?, ?> relationshipEntry = new DirectResponseRelationshipEntry(relationshipInstanceBuilder) {

				@Override
				public Class<?> getTargetAffiliation() {
					return relationshipRepositoryRegistration.getTargetType();
				}
			};
			relationshipEntries.add(relationshipEntry);
		}
	}

	/**
	 * @return {@link Filter} added by all modules
	 */
	public List<Filter> getFilters() {
		return aggregatedModule.getFilters();
	}
	
	/**
	 * @return {@link Filter} added by all modules
	 */
	public List<RepositoryFilter> getRepositoryFilters() {
		return aggregatedModule.getRepositoryFilters();
	}

	/**
	 * @return combined {@link ExceptionMapperLookup} added by all modules
	 */
	public ExceptionMapperLookup getExceptionMapperLookup() {
		return new CombinedExceptionMapperLookup(aggregatedModule.getExceptionMapperLookups());
	}

	public List<Module> getModules() {
		return modules;
	}

	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}
}
