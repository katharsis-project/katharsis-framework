package io.katharsis.rs;

import java.io.Serializable;
import java.util.Collection;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.boot.KatharsisBoot;
import io.katharsis.core.internal.boot.PropertiesProvider;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.module.Module;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.rs.internal.JaxrsModule;
import io.katharsis.rs.internal.parameterProvider.RequestContextParameterProviderRegistry;
import io.katharsis.rs.internal.parameterProvider.RequestContextParameterProviderRegistryBuilder;
import io.katharsis.rs.resource.registry.UriInfoServiceUrlProvider;

/**
 * Basic Katharsis feature that initializes core classes and provides a starting point to use the framework in
 * another projects.
 * <p>
 * This feature has NO {@link Provider} annotation, thus it require to provide an instance of  {@link ObjectMapper} and
 * {@link JsonServiceLocator} to provide instances of resources.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class KatharsisFeature implements Feature {

	private KatharsisBoot boot = new KatharsisBoot();

	@Context
	private SecurityContext securityContext;

	public KatharsisFeature() {
		// nothing to do
	}

	public KatharsisFeature(ObjectMapper objectMapper, QueryParamsBuilder queryParamsBuilder,
			JsonServiceLocator jsonServiceLocator) {
		boot.setObjectMapper(objectMapper);
		boot.setQueryParamsBuilds(queryParamsBuilder);
		boot.setServiceLocator(jsonServiceLocator);
	}

	public KatharsisFeature(ObjectMapper objectMapper, QuerySpecDeserializer querySpecDeserializer,
			JsonServiceLocator jsonServiceLocator) {
		boot.setObjectMapper(objectMapper);
		boot.setQuerySpecDeserializer(querySpecDeserializer);
		boot.setServiceLocator(jsonServiceLocator);
	}

	/**
	 * Sets a custom ServiceUrlProvider.
	 * 
	 * @param serviceUrlProvider
	 */
	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		boot.setServiceUrlProvider(serviceUrlProvider);
	}

	public void addModule(Module module) {
		boot.addModule(module);
	}

	@Override
	public boolean configure(final FeatureContext context) {
		ObjectMapper objectMapper = boot.getObjectMapper();
		ResourceFieldNameTransformer resourceFieldNameTransformer = new ResourceFieldNameTransformer(
				objectMapper.getSerializationConfig());

		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				return (String) context.getConfiguration().getProperty(key);
			}
		};

		boot.setDefaultServiceUrlProvider(new UriInfoServiceUrlProvider());
		boot.setPropertiesProvider(propertiesProvider);
		boot.setResourceFieldNameTransformer(resourceFieldNameTransformer);
		boot.addModule(new JaxrsModule(securityContext));
		boot.boot();

		KatharsisFilter katharsisFilter;
		try {
			RequestContextParameterProviderRegistry parameterProviderRegistry = buildParameterProviderRegistry();

			String webPathPrefix = boot.getWebPathPrefix();
			ResourceRegistry resourceRegistry = boot.getResourceRegistry();
			RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
			katharsisFilter = createKatharsisFilter(resourceRegistry, parameterProviderRegistry, webPathPrefix,
					requestDispatcher);
		}
		catch (Exception e) {
			throw new WebApplicationException(e);
		}
		context.register(katharsisFilter);
		
		registerActionRepositories(context, boot);

		return true;
	}

	/**
	 * All repositories with JAX-RS action need to be registered with JAX-RS as singletons.
	 * 
	 * @param context of jaxrs
	 * @param boot of katharsis
	 */
	private void registerActionRepositories(FeatureContext context, KatharsisBoot boot) {
		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		Collection<RegistryEntry> registryEntries = resourceRegistry.getResources();
		for(RegistryEntry registryEntry : registryEntries){
			ResourceRepositoryInformation repositoryInformation = registryEntry.getRepositoryInformation();
			if(!repositoryInformation.getActions().isEmpty()){
				ResourceRepositoryAdapter<?, Serializable> repositoryAdapter = registryEntry.getResourceRepository(null);
				Object resourceRepository = repositoryAdapter.getResourceRepository();
				context.register(resourceRepository);
			}
		}		
	}

	private RequestContextParameterProviderRegistry buildParameterProviderRegistry() {
		RequestContextParameterProviderRegistryBuilder builder = new RequestContextParameterProviderRegistryBuilder();
		return builder.build(boot.getServiceDiscovery());
	}

	protected KatharsisFilter createKatharsisFilter(ResourceRegistry resourceRegistry,
			RequestContextParameterProviderRegistry parameterProviderRegistry, String webPathPrefix,
			RequestDispatcher requestDispatcher) {
		return new KatharsisFilter(boot.getObjectMapper(), resourceRegistry, requestDispatcher, parameterProviderRegistry,
				webPathPrefix);
	}
	
	public ObjectMapper getObjectMapper(){
		return boot.getObjectMapper();
	}
	
	public void setDefaultPageLimit(Long defaultPageLimit){
		boot.setDefaultPageLimit(defaultPageLimit);
	}

	public QuerySpecDeserializer getQuerySpecDeserializer() {
		return boot.getQuerySpecDeserializer();
	}
	
	public KatharsisBoot getBoot(){
	  return boot;
	}
}
