package io.katharsis.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.exception.ExceptionMapperLookup;
import io.katharsis.core.internal.registry.DirectResponseRelationshipEntry;
import io.katharsis.core.internal.registry.DirectResponseResourceEntry;
import io.katharsis.core.internal.repository.information.ResourceRepositoryInformationImpl;
import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.core.internal.utils.Decorator;
import io.katharsis.core.internal.utils.MultivaluedMap;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.legacy.registry.AnnotatedRelationshipEntryBuilder;
import io.katharsis.legacy.registry.AnnotatedResourceEntry;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.legacy.registry.RepositoryInstanceBuilder;
import io.katharsis.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.legacy.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.repository.decorate.RelationshipRepositoryDecorator;
import io.katharsis.repository.decorate.RepositoryDecoratorFactory;
import io.katharsis.repository.decorate.ResourceRepositoryDecorator;
import io.katharsis.repository.filter.DocumentFilter;
import io.katharsis.repository.filter.RepositoryFilter;
import io.katharsis.repository.information.RelationshipRepositoryInformation;
import io.katharsis.repository.information.RepositoryInformation;
import io.katharsis.repository.information.RepositoryInformationBuilder;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;
import io.katharsis.resource.registry.MultiResourceLookup;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;
import io.katharsis.resource.registry.ResponseRelationshipEntry;
import io.katharsis.security.SecurityProvider;
import io.katharsis.utils.parser.TypeParser;

/**
 * Container for setting up and holding {@link Module} instances;
 */
public class ModuleRegistry {

	private TypeParser typeParser = new TypeParser();

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private List<Module> modules = new ArrayList<>();

	private SimpleModule aggregatedModule = new SimpleModule(null);

	private volatile boolean initialized;

	private ServiceDiscovery serviceDiscovery;

	private boolean isServer = true;

	public ModuleRegistry() {
		this(true);
	}

	public ModuleRegistry(boolean isServer) {
		this.isServer = isServer;
	}

	/**
	 * Register an new module to this registry and setup the module.
	 * 
	 * @param module
	 *            module
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
		public void addRepositoryInformationBuilder(RepositoryInformationBuilder repositoryInformationBuilder) {
			checkNotInitialized();
			aggregatedModule.addRepositoryInformationBuilder(repositoryInformationBuilder);
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
		public void addFilter(DocumentFilter filter) {
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
			aggregatedModule.addRepository(repository);
		}

		@Override
		public void addRepository(Class<?> sourceType, Class<?> targetType, Object repository) {
			checkNotInitialized();
			aggregatedModule.addRepository(repository);
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

		@Override
		public void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decoratorFactory) {
			checkNotInitialized();
			aggregatedModule.addRepositoryDecoratorFactory(decoratorFactory);
		}

		@Override
		public void addRepository(Object repository) {
			aggregatedModule.addRepository(repository);
		}

		@Override
		public boolean isServer() {
			return isServer;
		}

		@Override
		public TypeParser getTypeParser() {
			return typeParser;
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
	 * Ensures the {@link ModuleRegistry#init(ObjectMapper, ResourceRegistry)}
	 * has not yet been called.
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
		CombinedResourceInformationBuilder resourceInformationBuilder = new CombinedResourceInformationBuilder(aggregatedModule.getResourceInformationBuilders());
		DefaultResourceInformationBuilderContext context = new DefaultResourceInformationBuilderContext(resourceInformationBuilder, typeParser);
		resourceInformationBuilder.init(context);
		return resourceInformationBuilder;
	}

	/**
	 * Returns a {@link ResourceRepositoryBuilder} instance that combines all
	 * instances registered by modules.
	 *
	 * @return repository information builder
	 */
	public RepositoryInformationBuilder getRepositoryInformationBuilder() {
		return new CombinedRepositoryInformationBuilder(aggregatedModule.getRepositoryInformationBuilders());
	}

	/**
	 * Returns a {@link ResourceLookup} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public ResourceLookup getResourceLookup() {
		return new MultiResourceLookup(aggregatedModule.getResourceLookups());
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public SecurityProvider getSecurityProvider() {
		List<SecurityProvider> securityProviders = aggregatedModule.getSecurityProviders();
		PreconditionUtil.assertEquals("exactly one security provide must be installed, got: " + securityProviders, 1, securityProviders.size());
		return securityProviders.get(0);
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public ServiceDiscovery getServiceDiscovery() {
		PreconditionUtil.assertNotNull("serviceDiscovery not yet available", serviceDiscovery);
		return serviceDiscovery;
	}

	/**
	 * Combines all {@link ResourceInformationBuilder} instances provided by the
	 * registered {@link Module}.
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
			throw new UnsupportedOperationException("no ResourceInformationBuilder for " + resourceClass.getName() + " available");
		}

		@Override
		public String getResourceType(Class<?> resourceClass) {
			for (ResourceInformationBuilder builder : builders) {
				if (builder.accept(resourceClass)) {
					return builder.getResourceType(resourceClass);
				}
			}
			throw new UnsupportedOperationException("no ResourceInformationBuilder for " + resourceClass.getName() + " available");
		}

		@Override
		public void init(ResourceInformationBuilderContext context) {
			for (ResourceInformationBuilder builder : builders) {
				builder.init(context);
			}
		}
	}

	/**
	 * Combines all {@link RepositoryInformationBuilder} instances provided by
	 * the registered {@link Module}.
	 */
	static class CombinedRepositoryInformationBuilder implements RepositoryInformationBuilder {

		private Collection<RepositoryInformationBuilder> builders;

		public CombinedRepositoryInformationBuilder(List<RepositoryInformationBuilder> builders) {
			this.builders = builders;
		}

		@Override
		public boolean accept(Object repository) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repository)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public RepositoryInformation build(Object repository, RepositoryInformationBuilderContext context) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repository)) {
					return builder.build(repository, context);
				}
			}
			throw new UnsupportedOperationException("no RepositoryInformationBuilder for " + repository.getClass().getName() + " available");
		}

		@Override
		public boolean accept(Class<?> repositoryClass) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repositoryClass)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repositoryClass)) {
					return builder.build(repositoryClass, context);
				}
			}
			throw new UnsupportedOperationException("no RepositoryInformationBuilder for " + repositoryClass.getName() + " available");
		}
	}

	/**
	 * Combines all {@link ExceptionMapperLookup} instances provided by the
	 * registered {@link Module}.
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
	 * Initializes the {@link ModuleRegistry} and applies all pending changes.
	 * After the initialization completed, it is not possible to add any further
	 * modules.
	 * 
	 * @param objectMapper
	 *            object mapper
	 * @param resourceRegistry
	 *            resource registry
	 */
	public void init(ObjectMapper objectMapper) {
		if (!initialized) {
			this.initialized = true;
			this.objectMapper = objectMapper;
			this.objectMapper.registerModules(getJacksonModules());

			applyRepositoryRegistration(resourceRegistry);

			for (Module module : modules) {
				if (module instanceof InitializingModule) {
					((InitializingModule) module).init();
				}
			}
		}
	}

	public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void applyRepositoryRegistration(ResourceRegistry resourceRegistry) {
		List<Object> repositories = aggregatedModule.getRepositories();

		RepositoryInformationBuilder repositoryInformationBuilder = getRepositoryInformationBuilder();
		RepositoryInformationBuilderContext builderContext = new RepositoryInformationBuilderContext() {

			@Override
			public ResourceInformationBuilder getResourceInformationBuilder() {
				return ModuleRegistry.this.getResourceInformationBuilder();
			}

			@Override
			public TypeParser getTypeParser() {
				return typeParser;
			}
		};

		MultivaluedMap<Class<?>, RepositoryInformation> repositoryMap = new MultivaluedMap<>();
		Map<RepositoryInformation, Object> repositoryImplementations = new HashMap<>();

		for (Object repository : repositories) {
			if (!(repository instanceof ResourceRepositoryDecorator) && !(repository instanceof RelationshipRepositoryDecorator)) {
				RepositoryInformation repositoryInformation = repositoryInformationBuilder.build(repository, builderContext);
				if (repositoryInformation instanceof ResourceRepositoryInformation) {
					ResourceRepositoryInformation info = (ResourceRepositoryInformation) repositoryInformation;
					repositoryImplementations.put(info, repository);
					repositoryMap.add(info.getResourceInformation().getResourceClass(), repositoryInformation);
				} else {
					RelationshipRepositoryInformation info = (RelationshipRepositoryInformation) repositoryInformation;
					repositoryImplementations.put(info, repository);
					repositoryMap.add(info.getSourceResourceInformation().getResourceClass(), repositoryInformation);
				}
			}
		}

		for (Class<?> resourceClass : repositoryMap.keySet()) {
			ResourceRepositoryInformation resourceRepositoryInformation = null;
			List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>();
			ResourceEntry resourceEntry = null;
			List<RepositoryInformation> repositoryInformations = repositoryMap.getList(resourceClass);
			for (RepositoryInformation repositoryInformation : repositoryInformations) {
				if (repositoryInformation instanceof ResourceRepositoryInformation) {
					resourceRepositoryInformation = (ResourceRepositoryInformation) repositoryInformation;
					Object repository = repositoryImplementations.get(resourceRepositoryInformation);
					resourceEntry = setupResourceRepository(resourceRepositoryInformation, repository);
				} else {
					RelationshipRepositoryInformation relationshipRepositoryInformation = (RelationshipRepositoryInformation) repositoryInformation;
					Object repository = repositoryImplementations.get(repositoryInformation);
					setupRelationship(relationshipEntries, relationshipRepositoryInformation, repository);
				}
			}

			if (resourceRepositoryInformation == null) {

				ResourceInformationBuilder resourceInformationBuilder = getResourceInformationBuilder();
				DefaultResourceInformationBuilderContext context = new DefaultResourceInformationBuilderContext(resourceInformationBuilder, typeParser);

				ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);
				resourceRepositoryInformation = new ResourceRepositoryInformationImpl(resourceClass, resourceInformation.getResourceType(), resourceInformation);
			}

			RegistryEntry registryEntry = new RegistryEntry(resourceRepositoryInformation, resourceEntry, relationshipEntries);
			resourceRegistry.addEntry(resourceClass, registryEntry);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ResourceEntry setupResourceRepository(ResourceRepositoryInformation resourceRepositoryInformation, Object repository) {
		final Object decoratedRepository = decorateRepository(repository);
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, null) {

			@Override
			public Object buildRepository() {
				return decoratedRepository;
			}
		};

		if (ClassUtils.getAnnotation(decoratedRepository.getClass(), JsonApiResourceRepository.class).isPresent()) {
			return new AnnotatedResourceEntry(this, repositoryInstanceBuilder);
		} else {
			return new DirectResponseResourceEntry(repositoryInstanceBuilder);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object decorateRepository(Object repository) {
		Object decoratedRepository = repository;
		List<RepositoryDecoratorFactory> repositoryDecorators = getRepositoryDecoratorFactories();
		for (RepositoryDecoratorFactory repositoryDecorator : repositoryDecorators) {
			Decorator decorator = null;
			if (decoratedRepository instanceof RelationshipRepositoryV2) {
				decorator = repositoryDecorator.decorateRepository((RelationshipRepositoryV2) decoratedRepository);
			} else if (decoratedRepository instanceof ResourceRepositoryV2) {
				decorator = repositoryDecorator.decorateRepository((ResourceRepositoryV2) decoratedRepository);
			}
			if (decorator != null) {
				decorator.setDecoratedObject(decoratedRepository);
				decoratedRepository = decorator;
			}
		}
		if (decoratedRepository instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) decoratedRepository).setResourceRegistry(resourceRegistry);
		}
		return decoratedRepository;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupRelationship(List<ResponseRelationshipEntry> relationshipEntries, final RelationshipRepositoryInformation relationshipRepositoryInformation, final Object relRepository) {

		final Object decoratedRepository = decorateRepository(relRepository);
		RepositoryInstanceBuilder<Object> relationshipInstanceBuilder = new RepositoryInstanceBuilder<Object>(null, null) {

			@Override
			public Object buildRepository() {
				return decoratedRepository;
			}

			@Override
			public Class getRepositoryClass() {
				return relationshipRepositoryInformation.getRepositoryClass();
			}
		};

		if (ClassUtils.getAnnotation(relRepository.getClass(), JsonApiRelationshipRepository.class).isPresent()) {
			relationshipEntries.add(new AnnotatedRelationshipEntryBuilder(this, relationshipInstanceBuilder));
		} else {
			ResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(relationshipInstanceBuilder) {

				@Override
				public Class<?> getTargetAffiliation() {
					return relationshipRepositoryInformation.getResourceInformation().getResourceClass();
				}
			};
			relationshipEntries.add(relationshipEntry);
		}
	}

	/**
	 * @return {@link DocumentFilter} added by all modules
	 */
	public List<DocumentFilter> getFilters() {
		return aggregatedModule.getFilters();
	}

	/**
	 * @return {@link RepositoryFilter} added by all modules
	 */
	public List<RepositoryFilter> getRepositoryFilters() {
		return aggregatedModule.getRepositoryFilters();
	}

	/**
	 * @return {@link RepositoryDecoratorFactory} added by all modules
	 */
	public List<RepositoryDecoratorFactory> getRepositoryDecoratorFactories() {
		return aggregatedModule.getRepositoryDecoratorFactories();
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

	public TypeParser getTypeParser() {
		return typeParser;
	}
}
