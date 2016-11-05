package io.katharsis.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapter;
import io.katharsis.client.internal.BaseResponseDeserializer;
import io.katharsis.client.internal.ErrorResponseDeserializer;
import io.katharsis.client.internal.RelationshipRepositoryStubImpl;
import io.katharsis.client.internal.ResourceRepositoryStubImpl;
import io.katharsis.client.module.HttpAdapterAware;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.module.CoreModule;
import io.katharsis.module.Module;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.DirectResponseRelationshipEntry;
import io.katharsis.resource.registry.repository.DirectResponseResourceEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import io.katharsis.resource.registry.repository.ResponseRelationshipEntry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.JsonApiUrlBuilder;
import okhttp3.OkHttpClient;

/**
 * Client implementation giving access to JSON API repositories using stubs.
 */
public class KatharsisClient {

	private OkHttpAdapter httpAdapter;

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	private JsonApiUrlBuilder urlBuilder;

	private boolean initialized = false;

	private ExceptionMapperRegistry exceptionMapperRegistry;

	private boolean pushAlways = true;

	/**
	 * @param serviceUrl service url
	 * @param resourceSearchPackage search package
	 */
	public KatharsisClient(String serviceUrl, String resourceSearchPackage) {
		httpAdapter = new OkHttpAdapter();

		moduleRegistry = new ModuleRegistry();
		moduleRegistry.addModule(new CoreModule(resourceSearchPackage, new ResourceFieldNameTransformer()));

		resourceRegistry = new ResourceRegistry(moduleRegistry, new ConstantServiceUrlProvider(normalize(serviceUrl)));
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);

		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);


		// consider use of katharsis module in the future
		JsonApiModuleBuilder moduleBuilder = new JsonApiModuleBuilder();
		SimpleModule jsonApiModule = moduleBuilder.build(resourceRegistry, true);
		jsonApiModule.addDeserializer(BaseResponseContext.class, new BaseResponseDeserializer(resourceRegistry, objectMapper));
		jsonApiModule.addDeserializer(ErrorResponse.class, new ErrorResponseDeserializer());
		objectMapper.registerModule(jsonApiModule);
	}

	/**
	 * Older KatharsisClient implementation only supported a save() operation that POSTs the resource to the server. No difference is made
	 * between insert and update. The server-implementation still does not make a difference. 
	 * 
	 * By default the flag is enabled to maintain backward compatibility. But it is strongly adviced to turn id on. It will become
	 * the default in one of the subsequent releases.
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
		}
		else {
			return serviceUrl;
		}
	}

	protected void init() {
		if (initialized)
			return;
		initialized = true;

		initModuleRegistry();
		initRepositories();
		initExceptionMapperRegistry();
	}

	private void initModuleRegistry() {
		moduleRegistry.init(objectMapper);
	}

	private void initRepositories() {
		// register all resources
		ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();
		Set<Class<?>> resourceClasses = resourceLookup.getResourceClasses();
		for (Class<?> resourceClass : resourceClasses) {
			allocateRepository(resourceClass);
		}
	}

	private void initExceptionMapperRegistry() {
		ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
		exceptionMapperRegistry = new ExceptionMapperRegistryBuilder().build(exceptionMapperLookup);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T, I extends Serializable> void allocateRepository(Class<T> resourceClass) {
		ResourceInformation resourceInformation = moduleRegistry.getResourceInformationBuilder().build(resourceClass);
		final ResourceRepositoryStub<T, I> repositoryStub = new ResourceRepositoryStubImpl<>(this, resourceClass,
				resourceInformation, urlBuilder);

		// create interface for it!
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, null) {

			@Override
			public Object buildRepository() {
				return repositoryStub;
			}
		};
		ResourceEntry<T, I> resourceEntry = new DirectResponseResourceEntry<>(repositoryInstanceBuilder);
		Set<ResourceField> relationshipFields = resourceInformation.getRelationshipFields();
		List<ResponseRelationshipEntry<T, ?>> relationshipEntries = new ArrayList<>();
		RegistryEntry<T> registryEntry = new RegistryEntry<>(resourceInformation, resourceEntry, relationshipEntries);

		for (ResourceField relationshipField : relationshipFields) {
			final Class<?> targetClass = relationshipField.getElementType();

			final RelationshipRepositoryStubImpl relationshipRepositoryStub = new RelationshipRepositoryStubImpl(this,
					resourceClass, targetClass, resourceInformation, urlBuilder, registryEntry);
			RepositoryInstanceBuilder<RelationshipRepository> relationshipRepositoryInstanceBuilder = new RepositoryInstanceBuilder<RelationshipRepository>(
					null, null) {

				@Override
				public RelationshipRepository buildRepository() {
					return relationshipRepositoryStub;
				}
			};
			DirectResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(
					relationshipRepositoryInstanceBuilder) {

				@Override
				public Class<?> getTargetAffiliation() {
					return targetClass;
				}
			};
			relationshipEntries.add(relationshipEntry);
		}
		resourceRegistry.addEntry(resourceClass, registryEntry);
	}

	/**
	 * @param resourceClass resource class
	 * @return stub for the given resourceClass
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I extends Serializable> ResourceRepositoryStub<T, I> getRepository(Class<T> resourceClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(resourceClass);

		// TODO fix this in katharsis, should be able to get original resource
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (ResourceRepositoryStub<T, I>) repositoryAdapter.getResourceRepository();
	}

	/**
	 * @param resourceClass resource class
	 * @return stub for the given resourceClass
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I extends Serializable> QuerySpecResourceRepositoryStub<T, I> getQuerySpecRepository(Class<T> resourceClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(resourceClass);

		// TODO fix this in katharsis, should be able to get original resource
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (QuerySpecResourceRepositoryStub<T, I>) repositoryAdapter.getResourceRepository();
	}

	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target
	 *         class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryStub<T, I, D, J> getRepository(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (RelationshipRepositoryStub<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
	}

	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target
	 *         class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I extends Serializable, D, J extends Serializable> QuerySpecRelationshipRepositoryStub<T, I, D, J> getQuerySpecRepository(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (QuerySpecRelationshipRepositoryStub<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
	}

	/**
	 * @return objectMapper in use
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * @return http client library in use
	 */
	public OkHttpClient getHttpClient() {
		return httpAdapter.getImplementation();
	}

	public void setHttpClient(HttpAdapter httpAdapter) {
		this.httpAdapter = (OkHttpAdapter) httpAdapter;

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
}
