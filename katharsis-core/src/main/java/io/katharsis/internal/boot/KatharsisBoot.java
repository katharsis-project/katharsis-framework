package io.katharsis.internal.boot;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.module.Module;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.module.ServiceDiscovery;
import io.katharsis.module.ServiceDiscoveryFactory;
import io.katharsis.module.SimpleModule;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.queryspec.internal.QueryAdapterBuilder;
import io.katharsis.queryspec.internal.QueryParamsAdapterBuilder;
import io.katharsis.queryspec.internal.QuerySpecAdapterBuilder;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.Repository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PreconditionUtil;
import io.katharsis.utils.parser.TypeParser;
import net.jodah.typetools.TypeResolver;

/**
 * Facilitates the startup of Katharsis in various environments (Spring, CDI, JAX-RS, etc.).
 */
@SuppressWarnings("deprecation")
public class KatharsisBoot {

	private final ModuleRegistry moduleRegistry = new ModuleRegistry();

	private ObjectMapper objectMapper;

	private QueryParamsBuilder queryParamsBuilder;

	private QuerySpecDeserializer querySpecDeserializer = new DefaultQuerySpecDeserializer();

	private ServiceUrlProvider serviceUrlProvider;

	private boolean configured;

	private JsonServiceLocator serviceLocator;

	private ResourceRegistry resourceRegistry;

	private RequestDispatcher requestDispatcher;

	private PropertiesProvider propertiesProvider;

	private ResourceFieldNameTransformer resourceFieldNameTransformer;

	private ServiceUrlProvider defaultServiceUrlProvider;

	private ServiceDiscoveryFactory serviceDiscoveryFactory = new DefaultServiceDiscoveryFactory();

	public void setObjectMapper(ObjectMapper objectMapper) {
		PreconditionUtil.assertNull("ObjectMapper already set", this.objectMapper);
		this.objectMapper = objectMapper;
	}

	public void setServiceDiscoveryFactory(ServiceDiscoveryFactory factory) {
		this.serviceDiscoveryFactory = factory;
	}

	public void setQueryParamsBuilds(QueryParamsBuilder queryParamsBuilder) {
		this.queryParamsBuilder = queryParamsBuilder;
		this.querySpecDeserializer = null;
	}

	public void setQuerySpecDeserializer(QuerySpecDeserializer querySpecDeserializer) {
		this.querySpecDeserializer = querySpecDeserializer;
		this.queryParamsBuilder = null;
	}

	/**
	 * Sets a JsonServiceLocator.  No longer necessary if a ServiceDiscovery implementation is in place.
	 * 
	 * @param serviceLocator Ask Remmo
	 */
	public void setServiceLocator(JsonServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	/**
	 * Adds a module. No longer necessary if a ServiceDiscovery implementation is in place.
	 * 
	 * @param module Ask Remmo
	 */
	public void addModule(Module module) {
		moduleRegistry.addModule(module);
	}

	/**
	* Sets a ServiceUrlProvider.  No longer necessary if a ServiceDiscovery implementation is in place.
	* 
	* @param serviceUrlProvider Ask Remmo
	*/
	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		checkNotConfiguredYet();
		this.serviceUrlProvider = serviceUrlProvider;
	}

	private void checkNotConfiguredYet() {
		if (configured) {
			throw new IllegalStateException("cannot further modify KatharsisFeature once configured/initialized by JAX-RS");
		}
	}

	/**
	 * Performs the setup.
	 */
	public void boot() {
		configured = true;

		setupServiceUrlProvider();
		setupServiceDiscovery();
		bootDiscovery();
	}

	private void setupServiceDiscovery() {
		// revert to reflection-based approach if no ServiceDiscovery is found
		FallbackServiceDiscoveryFactory fallback = new FallbackServiceDiscoveryFactory(serviceDiscoveryFactory, serviceLocator,
				propertiesProvider);
		moduleRegistry.setServiceDiscovery(fallback.getInstance());
	}

	private void bootDiscovery() {
		addModules();
		setupComponents();
		resourceRegistry = new ResourceRegistry(moduleRegistry, serviceUrlProvider);

		moduleRegistry.init(objectMapper);

		JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
		objectMapper.registerModule(jsonApiModuleBuilder.build(resourceRegistry, false));

		ExceptionMapperRegistry exceptionMapperRegistry = buildExceptionMapperRegistry();

		requestDispatcher = createRequestDispatcher(exceptionMapperRegistry);

	}



	private RequestDispatcher createRequestDispatcher(ExceptionMapperRegistry exceptionMapperRegistry) {
		TypeParser typeParser = new TypeParser();
		ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder(resourceRegistry, typeParser,
				objectMapper);
		ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();

		QueryAdapterBuilder queryAdapterBuilder;
		if (queryParamsBuilder != null) {
			queryAdapterBuilder = new QueryParamsAdapterBuilder(queryParamsBuilder, resourceRegistry);
		}
		else {
			queryAdapterBuilder = new QuerySpecAdapterBuilder(querySpecDeserializer, resourceRegistry);
		}

		return new RequestDispatcher(moduleRegistry, controllerRegistry, exceptionMapperRegistry, queryAdapterBuilder);
	}


	private ExceptionMapperRegistry buildExceptionMapperRegistry() {
		ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
		ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
		return mapperRegistryBuilder.build(exceptionMapperLookup);
	}

	@SuppressWarnings("rawtypes")
	private void setupComponents() {
		ServiceDiscovery serviceDiscovery = moduleRegistry.getServiceDiscovery();
		SimpleModule module = new SimpleModule("discovery");

		module.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(resourceFieldNameTransformer));

		for (JsonApiExceptionMapper<?> exceptionMapper : serviceDiscovery.getInstancesByType(JsonApiExceptionMapper.class)) {
			module.addExceptionMapper(exceptionMapper);
		}
		for (Filter filter : serviceDiscovery.getInstancesByType(Filter.class)) {
			module.addFilter(filter);
		}

		for (Object repository : serviceDiscovery.getInstancesByType(Repository.class)) {
			setupRepository(module, repository);
		}
		for (Object repository : serviceDiscovery.getInstancesByAnnotation(JsonApiResourceRepository.class)) {
			JsonApiResourceRepository annotation = ClassUtils
					.getAnnotation(repository.getClass(), JsonApiResourceRepository.class).get();
			Class<?> resourceClass = annotation.value();
			module.addRepository(resourceClass, repository);
		}
		for (Object repository : serviceDiscovery.getInstancesByAnnotation(JsonApiRelationshipRepository.class)) {
			JsonApiRelationshipRepository annotation = ClassUtils
					.getAnnotation(repository.getClass(), JsonApiRelationshipRepository.class).get();
			module.addRepository(annotation.source(), annotation.target(), repository);
		}
		moduleRegistry.addModule(module);
	}

	private void setupRepository(SimpleModule module, Object repository) {
		if (repository instanceof ResourceRepository) {
			ResourceRepository resRepository = (ResourceRepository) repository;
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, resRepository.getClass());
			Class resourceClass = typeArgs[0];
			module.addRepository(resourceClass, resRepository);
		}
		else if (repository instanceof RelationshipRepository) {
			RelationshipRepository relRepository = (RelationshipRepository) repository;
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, relRepository.getClass());
			Class sourceResourceClass = typeArgs[0];
			Class targetResourceClass = typeArgs[2];
			module.addRepository(sourceResourceClass, targetResourceClass, relRepository);
		}
		else if (repository instanceof QuerySpecResourceRepository) {
			QuerySpecResourceRepository<?, ?> resRepository = (QuerySpecResourceRepository<?, ?>) repository;
			module.addRepository(resRepository.getResourceClass(), resRepository);
		}
		else if (repository instanceof QuerySpecRelationshipRepository) {
			QuerySpecRelationshipRepository<?, ?, ?, ?> relRepository = (QuerySpecRelationshipRepository<?, ?, ?, ?>) repository;
			module.addRepository(relRepository.getSourceResourceClass(), relRepository.getTargetResourceClass(), relRepository);
		}
		else {
			throw new IllegalStateException(repository.toString());
		}
	}

	private void addModules() {
		ServiceDiscovery serviceDiscovery = moduleRegistry.getServiceDiscovery();
		List<Module> modules = serviceDiscovery.getInstancesByType(Module.class);
		for (Module module : modules) {
			moduleRegistry.addModule(module);
		}
	}

	private void setupServiceUrlProvider() {
		if (serviceUrlProvider == null) {
			String resourceDefaultDomain = propertiesProvider.getProperty(KatharsisBootProperties.RESOURCE_DEFAULT_DOMAIN);
			String webPathPrefix = getWebPathPrefix();
			if (resourceDefaultDomain != null) {
				String serviceUrl = buildServiceUrl(resourceDefaultDomain, webPathPrefix);
				serviceUrlProvider = new ConstantServiceUrlProvider(serviceUrl);
			}
			else {
				// serviceUrl is obtained from incoming request context
				serviceUrlProvider = defaultServiceUrlProvider;
			}
		}
		PreconditionUtil.assertNotNull("expected serviceUrlProvider", serviceUrlProvider);
	}

	private static String buildServiceUrl(String resourceDefaultDomain, String webPathPrefix) {
		return resourceDefaultDomain + (webPathPrefix != null ? webPathPrefix : "");
	}

	public RequestDispatcher getRequestDispatcher() {
		PreconditionUtil.assertNotNull("expected requestDispatcher", requestDispatcher);
		return requestDispatcher;
	}

	public ResourceRegistry getResourceRegistry() {
		return resourceRegistry;
	}

	public ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}

	public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
		this.propertiesProvider = propertiesProvider;
	}

	public void setResourceFieldNameTransformer(ResourceFieldNameTransformer resourceFieldNameTransformer) {
		this.resourceFieldNameTransformer = resourceFieldNameTransformer;
	}

	public void setDefaultServiceUrlProvider(ServiceUrlProvider defaultServiceUrlProvider) {
		this.defaultServiceUrlProvider = defaultServiceUrlProvider;
	}

	public String getWebPathPrefix() {
		return propertiesProvider.getProperty(KatharsisBootProperties.WEB_PATH_PREFIX);
	}

	public ServiceDiscovery getServiceDiscovery() {
		return moduleRegistry.getServiceDiscovery();
	}
}
