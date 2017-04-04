package io.katharsis.client;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.katharsis.client.action.ActionStubFactory;
import io.katharsis.client.action.ActionStubFactoryContext;
import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.apache.HttpClientAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapter;
import io.katharsis.client.internal.ClientDocumentMapper;
import io.katharsis.client.internal.ClientStubInvocationHandler;
import io.katharsis.client.internal.RelationshipRepositoryStubImpl;
import io.katharsis.client.internal.ResourceRepositoryStubImpl;
import io.katharsis.client.internal.proxy.BasicProxyFactory;
import io.katharsis.client.internal.proxy.ClientProxyFactory;
import io.katharsis.client.internal.proxy.ClientProxyFactoryContext;
import io.katharsis.client.module.ClientModule;
import io.katharsis.client.module.HttpAdapterAware;
import io.katharsis.core.internal.exception.ExceptionMapperLookup;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.exception.ExceptionMapperRegistryBuilder;
import io.katharsis.core.internal.jackson.JsonApiModuleBuilder;
import io.katharsis.core.internal.registry.DirectResponseRelationshipEntry;
import io.katharsis.core.internal.registry.DirectResponseResourceEntry;
import io.katharsis.core.internal.registry.ResourceRegistryImpl;
import io.katharsis.core.internal.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.internal.repository.information.ResourceRepositoryInformationImpl;
import io.katharsis.core.internal.utils.JsonApiUrlBuilder;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.errorhandling.exception.RepositoryNotFoundException;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.legacy.registry.RepositoryInstanceBuilder;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.module.Module;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.repository.information.RepositoryInformationBuilder;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResponseRelationshipEntry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.utils.parser.TypeParser;

/**
 * Client implementation giving access to JSON API repositories using stubs.
 */
public class KatharsisClient {

	private static final String APACHE_HTTP_CLIENT_DETECTION_CLASS = "org.apache.http.impl.client.CloseableHttpClient";

	private static final String OK_HTTP_CLIENT_DETECTION_CLASS = "okhttp3.OkHttpClient";

	private HttpAdapter httpAdapter;

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	private JsonApiUrlBuilder urlBuilder;

	private boolean initialized = false;

	private ExceptionMapperRegistry exceptionMapperRegistry;

	private boolean pushAlways = false;

	private ActionStubFactory actionStubFactory;

	private ClientDocumentMapper documentMapper;

	public KatharsisClient(String serviceUrl) {
		this(new ConstantServiceUrlProvider(normalize(serviceUrl)));
	}

	public KatharsisClient(ServiceUrlProvider serviceUrlProvider) {
		httpAdapter = detectHttpAdapter();

		moduleRegistry = new ModuleRegistry(false);

		moduleRegistry.addModule(new ClientModule());

		resourceRegistry = new ClientResourceRegistry(moduleRegistry, serviceUrlProvider);
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);

		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// consider use of katharsis module in the future
		JsonApiModuleBuilder moduleBuilder = new JsonApiModuleBuilder();
		SimpleModule jsonApiModule = moduleBuilder.build(resourceRegistry, true);
		objectMapper.registerModule(jsonApiModule);

		documentMapper = new ClientDocumentMapper(moduleRegistry, objectMapper, null);
		setProxyFactory(new BasicProxyFactory());
	}

	public void setProxyFactory(ClientProxyFactory proxyFactory) {
		proxyFactory.init(new ClientProxyFactoryContext() {

			@Override
			public ModuleRegistry getModuleRegistry() {
				return moduleRegistry;
			}

			@Override
			public <T> DefaultResourceList<T> getCollection(Class<T> resourceClass, String url) {
				RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
				ResourceInformation resourceInformation = entry.getResourceInformation();
				final ResourceRepositoryStubImpl<T, ?> repositoryStub = new ResourceRepositoryStubImpl<>(KatharsisClient.this, resourceClass, resourceInformation, urlBuilder);
				return repositoryStub.findAll(url);

			}
		});
		documentMapper.setProxyFactory(proxyFactory);
	}

	private HttpAdapter detectHttpAdapter() {
		if (existsClass(OK_HTTP_CLIENT_DETECTION_CLASS)) {
			return OkHttpAdapter.newInstance();
		}
		if (existsClass(APACHE_HTTP_CLIENT_DETECTION_CLASS)) {
			return HttpClientAdapter.newInstance();
		}
		throw new IllegalStateException("no httpAdapter can be initialized, add okhttp3 (com.squareup.okhttp3:okhttp) or apache http client (org.apache.httpcomponents:httpclient) to the classpath");
	}

	private static boolean existsClass(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	class ClientResourceRegistry extends ResourceRegistryImpl {

		public ClientResourceRegistry(ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider) {
			super(moduleRegistry, serviceUrlProvider);
		}

		@Override
		protected synchronized RegistryEntry getEntry(Class<?> clazz, boolean allowNull) {
			RegistryEntry entry = resources.get(clazz);
			if (entry == null) {
				ResourceInformationBuilder informationBuilder = moduleRegistry.getResourceInformationBuilder();
				if (!informationBuilder.accept(clazz)) {
					throw new RepositoryNotFoundException(clazz.getName() + " not recognized as resource class, consider adding @JsonApiResource annotation");
				}
				entry = allocateRepository(clazz, true);
			}
			return entry;
		}

		public boolean isInitialized(Class<?> clazz) {
			return super.getEntry(clazz, true) != null;
		}
	}

	/**
	 * Older KatharsisClient implementation only supported a save() operation
	 * that POSTs the resource to the server. No difference is made between
	 * insert and update. The server-implementation still does not make a
	 * difference.
	 * 
	 * By default the flag is enabled to maintain backward compatibility. But it
	 * is strongly adviced to turn id on. It will become the default in one of
	 * the subsequent releases.
	 * 
	 * @param pushAlways
	 */
	public void setPushAlways(boolean pushAlways) {
		this.pushAlways = pushAlways;
	}

	public boolean getPushAlways() {
		return pushAlways;
	}

	private static String normalize(String serviceUrl) {
		if (serviceUrl.endsWith("/")) {
			return serviceUrl.substring(0, serviceUrl.length() - 1);
		} else {
			return serviceUrl;
		}
	}

	protected void init() {
		if (initialized)
			return;
		initialized = true;

		initModuleRegistry();
		initExceptionMapperRegistry();
		initResources();
	}

	private void initResources() {
		ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();
		for (Class<?> resourceClass : resourceLookup.getResourceClasses()) {
			getQuerySpecRepository(resourceClass);
		}
	}

	private void initModuleRegistry() {
		moduleRegistry.init(objectMapper);
	}

	private void initExceptionMapperRegistry() {
		ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
		exceptionMapperRegistry = new ExceptionMapperRegistryBuilder().build(exceptionMapperLookup);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T, I extends Serializable> RegistryEntry allocateRepository(Class<T> resourceClass, boolean allocateRelated) {
		ResourceInformationBuilder resourceInformationBuilder = moduleRegistry.getResourceInformationBuilder();
		DefaultResourceInformationBuilderContext context = new DefaultResourceInformationBuilderContext(resourceInformationBuilder, moduleRegistry.getTypeParser());

		ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);
		final ResourceRepositoryStub<T, I> repositoryStub = new ResourceRepositoryStubImpl<>(this, resourceClass, resourceInformation, urlBuilder);

		// create interface for it!
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, null) {

			@Override
			public Object buildRepository() {
				return repositoryStub;
			}
		};
		ResourceRepositoryInformation repositoryInformation = new ResourceRepositoryInformationImpl(repositoryStub.getClass(), resourceInformation.getResourceType(), resourceInformation);
		ResourceEntry resourceEntry = new DirectResponseResourceEntry(repositoryInstanceBuilder);
		List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>();
		RegistryEntry registryEntry = new RegistryEntry(repositoryInformation, resourceEntry, relationshipEntries);
		resourceRegistry.addEntry(resourceClass, registryEntry);

		allocateRepositoryRelations(registryEntry, allocateRelated, relationshipEntries);

		return registryEntry;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> void allocateRepositoryRelations(RegistryEntry registryEntry, boolean allocateRelated, List<ResponseRelationshipEntry> relationshipEntries) {
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		List<ResourceField> relationshipFields = resourceInformation.getRelationshipFields();
		for (ResourceField relationshipField : relationshipFields) {
			final Class<?> targetClass = relationshipField.getElementType();
			Class<?> resourceClass = resourceInformation.getResourceClass();

			final RelationshipRepositoryStubImpl relationshipRepositoryStub = new RelationshipRepositoryStubImpl(this, resourceClass, targetClass, resourceInformation, urlBuilder, registryEntry);
			RepositoryInstanceBuilder<RelationshipRepository> relationshipRepositoryInstanceBuilder = new RepositoryInstanceBuilder<RelationshipRepository>(null, null) {

				@Override
				public RelationshipRepository buildRepository() {
					return relationshipRepositoryStub;
				}
			};
			DirectResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(relationshipRepositoryInstanceBuilder) {

				@Override
				public Class<?> getTargetAffiliation() {
					return targetClass;
				}
			};
			relationshipEntries.add(relationshipEntry);

			// allocate relations as well
			if (allocateRelated) {
				ClientResourceRegistry clientResourceRegistry = (ClientResourceRegistry) resourceRegistry;
				if (!clientResourceRegistry.isInitialized(targetClass)) {
					allocateRepository(targetClass, true);
				}
			}
		}
	}

	/**
	 * @deprecated Make use of getRepositoryForInterface.
	 */
	@Deprecated
	public <R extends ResourceRepositoryV2<?, ?>> R getResourceRepository(Class<R> repositoryInterfaceClass) {
		return getRepositoryForInterface(repositoryInterfaceClass);
	}

	@SuppressWarnings("unchecked")
	public <R extends ResourceRepositoryV2<?, ?>> R getRepositoryForInterface(Class<R> repositoryInterfaceClass) {
		RepositoryInformationBuilder informationBuilder = moduleRegistry.getRepositoryInformationBuilder();
		PreconditionUtil.assertTrue("no a valid repository interface", informationBuilder.accept(repositoryInterfaceClass));
		ResourceRepositoryInformation repositoryInformation = (ResourceRepositoryInformation) informationBuilder.build(repositoryInterfaceClass, newRepositoryInformationBuilderContext());
		Class<?> resourceClass = repositoryInformation.getResourceInformation().getResourceClass();

		Object actionStub = actionStubFactory != null ? actionStubFactory.createStub(repositoryInterfaceClass) : null;
		ResourceRepositoryV2<?, Serializable> repositoryStub = getQuerySpecRepository(resourceClass);

		ClassLoader classLoader = repositoryInterfaceClass.getClassLoader();
		InvocationHandler invocationHandler = new ClientStubInvocationHandler(repositoryInterfaceClass, repositoryStub, actionStub);
		return (R) Proxy.newProxyInstance(classLoader, new Class[] { repositoryInterfaceClass, ResourceRepositoryV2.class }, invocationHandler);
	}

	private RepositoryInformationBuilderContext newRepositoryInformationBuilderContext() {
		return new RepositoryInformationBuilderContext() {

			@Override
			public ResourceInformationBuilder getResourceInformationBuilder() {
				return moduleRegistry.getResourceInformationBuilder();
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		};
	}

	/**
	 * @deprecated make use of QuerySpec
	 */
	@Deprecated
	public <R extends RelationshipRepositoryV2<?, ?, ?, ?>> R getQueryParamsRelationshipRepository(Class<R> repositoryInterfaceClass) {
		return null;
	}

	/**
	 * @param resourceClass
	 *            resource class
	 * @return stub for the given resourceClass
	 * 
	 * @deprecated make use of QuerySpec
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Deprecated
	public <T, I extends Serializable> ResourceRepositoryStub<T, I> getQueryParamsRepository(Class<T> resourceClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);

		// TODO fix this in katharsis, should be able to get original resource
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (ResourceRepositoryStub<T, I>) repositoryAdapter.getResourceRepository();
	}

	/**
	 * @param resourceClass
	 *            resource class
	 * @return stub for the given resourceClass
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I extends Serializable> ResourceRepositoryV2<T, I> getRepositoryForType(Class<T> resourceClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (ResourceRepositoryV2<T, I>) repositoryAdapter.getResourceRepository();

	}

	/**
	 * @deprecated make use of getRepositoryForType()
	 */
	@Deprecated
	public <T, I extends Serializable> ResourceRepositoryV2<T, I> getQuerySpecRepository(Class<T> resourceClass) {
		return getRepositoryForType(resourceClass);
	}

	/**
	 * @param sourceClass
	 *            source class
	 * @param targetClass
	 *            target class
	 * @return stub for the relationship between the given source and target
	 *         class
	 * 
	 * @deprecated make use of QuerySpec
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryStub<T, I, D, J> getQueryParamsRepository(Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (RelationshipRepositoryStub<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
	}

	/**
	 * @param sourceClass
	 *            source class
	 * @param targetClass
	 *            target class
	 * @return stub for the relationship between the given source and target
	 *         class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryV2<T, I, D, J> getRepositoryForType(Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (RelationshipRepositoryV2<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
	}

	/**
	 * @deprecated make use of getRepositoryForType()
	 */
	@Deprecated
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryV2<T, I, D, J> getQuerySpecRepository(Class<T> sourceClass, Class<D> targetClass) {
		return getRepositoryForType(sourceClass, targetClass);
	}

	/**
	 * @return objectMapper in use
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setHttpAdapter(HttpAdapter httpAdapter) {
		this.httpAdapter = httpAdapter;

		List<Module> modules = moduleRegistry.getModules();
		for (Module module : modules) {
			if (module instanceof HttpAdapterAware) {
				((HttpAdapterAware) module).setHttpAdapter(getHttpAdapter());
			}
		}
	}

	/**
	 * @return resource repository use.
	 */
	public ResourceRegistry getRegistry() {
		return resourceRegistry;
	}

	/**
	 * Adds the given module.
	 * 
	 * @param module
	 */
	public void addModule(Module module) {
		if (module instanceof HttpAdapterAware) {
			((HttpAdapterAware) module).setHttpAdapter(getHttpAdapter());
		}
		this.moduleRegistry.addModule(module);
	}

	public HttpAdapter getHttpAdapter() {
		return httpAdapter;
	}

	public ExceptionMapperRegistry getExceptionMapperRegistry() {
		return exceptionMapperRegistry;
	}

	public ActionStubFactory getActionStubFactory() {
		return actionStubFactory;
	}

	/**
	 * Sets the factory to use to create action stubs (like JAX-RS annotated
	 * repository methods).
	 * 
	 * @param actionStubFactory
	 *            to use
	 */
	public void setActionStubFactory(ActionStubFactory actionStubFactory) {
		this.actionStubFactory = actionStubFactory;
		if (actionStubFactory != null) {
			actionStubFactory.init(new ActionStubFactoryContext() {

				@Override
				public ServiceUrlProvider getServiceUrlProvider() {
					return moduleRegistry.getResourceRegistry().getServiceUrlProvider();
				}

				@Override
				public HttpAdapter getHttpAdapter() {
					return httpAdapter;
				}
			});
		}
	}

	public ModuleRegistry getModuleRegistry() {
		return moduleRegistry;
	}

	public ClientDocumentMapper getDocumentMapper() {
		return documentMapper;
	}
}
