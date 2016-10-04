package io.katharsis.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.squareup.okhttp.OkHttpClient;
import io.katharsis.client.internal.*;
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
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.JsonApiUrlBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 * Client implementation giving access to JSON API repositories using stubs.
 */
public class KatharsisClient {

	private OkHttpClient client = new OkHttpClient();
	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;
	private JsonApiUrlBuilder urlBuilder;

	private boolean initialized = false;

	private ExceptionMapperRegistry exceptionMapperRegistry;

	/**
	 * @param serviceUrl service url
	 * @param resourceSearchPackage search package
	 */
	public KatharsisClient(String serviceUrl, String resourceSearchPackage) {
		resourceRegistry = new ResourceRegistry(new ConstantServiceUrlProvider(normalize(serviceUrl)));
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);

		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		moduleRegistry = new ModuleRegistry();
		moduleRegistry.addModule(new CoreModule(resourceSearchPackage, new ResourceFieldNameTransformer()));

		// consider use of katharsis module in the future
		JsonApiModuleBuilder moduleBuilder = new JsonApiModuleBuilder();
		SimpleModule jsonApiModule = moduleBuilder.build(resourceRegistry, true);
		jsonApiModule.addDeserializer(BaseResponseContext.class,
				new BaseResponseDeserializer(resourceRegistry, objectMapper));
		jsonApiModule.addDeserializer(ErrorResponse.class, new ErrorResponseDeserializer());
		objectMapper.registerModule(jsonApiModule);
	}

	private static String normalize(String serviceUrl) {
		if (serviceUrl.endsWith("/")) {
			return serviceUrl.substring(0, serviceUrl.length() - 1);
		}else{
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
		moduleRegistry.init(objectMapper, resourceRegistry);
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
	private <T, ID extends Serializable> void allocateRepository(Class<T> resourceClass) {
		ResourceInformation resourceInformation = moduleRegistry.getResourceInformationBuilder().build(resourceClass);
		final ResourceRepositoryStub<T, ID> repositoryStub = new ResourceRepositoryStubImpl<>(this, resourceClass,
				resourceInformation, urlBuilder);

		// create interface for it!
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, null) {
			@Override
			public Object buildRepository() {
				return repositoryStub;
			}
		};
		ResourceEntry<T, ID> resourceEntry = new DirectResponseResourceEntry<T, ID>(repositoryInstanceBuilder);
		Set<ResourceField> relationshipFields = resourceInformation.getRelationshipFields();
		List<ResponseRelationshipEntry<T, ?>> relationshipEntries = new ArrayList<>();
		RegistryEntry<T> registryEntry = new RegistryEntry<T>(resourceInformation, resourceEntry, relationshipEntries);

		for (ResourceField relationshipField : relationshipFields) {
			final Class<?> targetClass = relationshipField.getType();
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
	public <T, ID extends Serializable> ResourceRepositoryStub<T, ID> getRepository(Class<T> resourceClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(resourceClass);

		// TODO fix this in katharsis, should be able to get original resource
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (ResourceRepositoryStub<T, ID>) repositoryAdapter.getResourceRepository();
	}
	
	/**
	 * @param resourceClass resource class
	 * @return stub for the given resourceClass
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, ID extends Serializable> QuerySpecResourceRepositoryStub<T, ID> getQuerySpecRepository(Class<T> resourceClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(resourceClass);

		// TODO fix this in katharsis, should be able to get original resource
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (QuerySpecResourceRepositoryStub<T, ID>) repositoryAdapter.getResourceRepository();
	}

	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target
	 *         class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, ID extends Serializable, D, DID extends Serializable> RelationshipRepositoryStub<T, ID, D, DID> getRepository(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (RelationshipRepositoryStub<T, ID, D, DID>) repositoryAdapter.getRelationshipRepository();
	}
	
	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target
	 *         class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, ID extends Serializable, D, DID extends Serializable> QuerySpecRelationshipRepositoryStub<T, ID, D, DID> getQuerySpecRepository(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry<T> entry = resourceRegistry.getEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (QuerySpecRelationshipRepositoryStub<T, ID, D, DID>) repositoryAdapter.getRelationshipRepository();
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
		return client;
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
		this.moduleRegistry.addModule(module);
	}

	public ExceptionMapperRegistry getExceptionMapperRegistry() {
		return exceptionMapperRegistry;
	}
}
