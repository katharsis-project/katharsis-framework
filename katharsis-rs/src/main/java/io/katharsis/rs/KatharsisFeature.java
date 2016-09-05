package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.module.CoreModule;
import io.katharsis.module.Module;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.registry.*;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderLookup;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderRegistry;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderRegistryBuilder;
import io.katharsis.utils.parser.TypeParser;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Basic Katharsis feature that initializes core classes and provides a starting point to use the framework in
 * another projects.
 * <p>
 * This feature has NO {@link Provider} annotation, thus it require to provide an instance of  {@link ObjectMapper} and
 * {@link JsonServiceLocator} to provide instances of resources.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class KatharsisFeature implements Feature {

	private final ModuleRegistry moduleRegistry;
    private final JsonServiceLocator jsonServiceLocator;
    private final ObjectMapper objectMapper;
    private final QueryParamsBuilder queryParamsBuilder;

    public KatharsisFeature(ObjectMapper objectMapper,
                            QueryParamsBuilder queryParamsBuilder,
                            JsonServiceLocator jsonServiceLocator) {
        this.objectMapper = objectMapper;
        this.queryParamsBuilder = queryParamsBuilder;
        this.jsonServiceLocator = jsonServiceLocator;
        this.moduleRegistry = new ModuleRegistry();
    }
    
    public void addModule(Module module){
    	moduleRegistry.addModule(module);
    }

    public ResourceLookup createResourceLookup(FeatureContext context) {
        String resourceSearchPackage = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);

        return new DefaultResourceLookup(resourceSearchPackage);
    }

    private RequestContextParameterProviderLookup createRequestContextProviderLookup(FeatureContext context) {
        String resourceSearchPackage = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);

        return new RequestContextParameterProviderLookup(resourceSearchPackage, jsonServiceLocator);
    }

    @Override
    public boolean configure(FeatureContext context) {
        String resourceDefaultDomain = (String) context
            .getConfiguration()
            .getProperty(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN);
        String webPathPrefix = (String) context
            .getConfiguration()
            .getProperty(KatharsisProperties.WEB_PATH_PREFIX);

        String resourceSearchPackage = (String) context.getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);
                
        ResourceFieldNameTransformer resourceFieldNameTransformer = new ResourceFieldNameTransformer(objectMapper.getSerializationConfig());
        moduleRegistry.addModule(new CoreModule(resourceSearchPackage, resourceFieldNameTransformer));
       
        String serviceUrl = buildServiceUrl(resourceDefaultDomain, webPathPrefix);
        ResourceLookup resourceLookup = createResourceLookup(context);
        ResourceRegistry resourceRegistry = buildResourceRegistry(resourceLookup, serviceUrl);

        moduleRegistry.init(objectMapper, resourceRegistry);
        
        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        objectMapper.registerModule(jsonApiModuleBuilder.build(resourceRegistry, false));

        KatharsisFilter katharsisFilter;
        try {
        	ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
            ExceptionMapperRegistry exceptionMapperRegistry = buildExceptionMapperRegistry(exceptionMapperLookup);
            RequestContextParameterProviderLookup containerRequestContextProviderLookup = createRequestContextProviderLookup(context);
            RequestContextParameterProviderRegistry parameterProviderRegistry = buildParameterProviderRegistry(containerRequestContextProviderLookup);
            RequestDispatcher requestDispatcher = createRequestDispatcher(resourceRegistry, exceptionMapperRegistry);

            katharsisFilter = createKatharsisFilter(resourceRegistry, parameterProviderRegistry, webPathPrefix, requestDispatcher);
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
        context.register(katharsisFilter);

        return true;
    }

    private RequestContextParameterProviderRegistry buildParameterProviderRegistry(RequestContextParameterProviderLookup containerRequestContextProviderLookup) {
        RequestContextParameterProviderRegistryBuilder builder = new RequestContextParameterProviderRegistryBuilder();
        return builder.build(containerRequestContextProviderLookup);
    }

    private static String buildServiceUrl(String resourceDefaultDomain, String webPathPrefix) {
        return resourceDefaultDomain + (webPathPrefix != null ? webPathPrefix : "");
    }

    private static ExceptionMapperRegistry buildExceptionMapperRegistry(ExceptionMapperLookup exceptionMapperLookup) throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(exceptionMapperLookup);
    }

    private ResourceRegistry buildResourceRegistry(ResourceLookup lookup, String serviceUrl) {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(jsonServiceLocator, moduleRegistry.getResourceInformationBuilder());
        return registryBuilder.build(lookup, new ConstantServiceUrlProvider(serviceUrl));
    }

    protected KatharsisFilter createKatharsisFilter(ResourceRegistry resourceRegistry,
        RequestContextParameterProviderRegistry parameterProviderRegistry, String webPathPrefix, RequestDispatcher requestDispatcher) throws Exception {
        return new KatharsisFilter(objectMapper, queryParamsBuilder, resourceRegistry, requestDispatcher, parameterProviderRegistry, webPathPrefix);
    }

    private RequestDispatcher createRequestDispatcher(ResourceRegistry resourceRegistry,
        ExceptionMapperRegistry exceptionMapperRegistry) throws Exception {
        TypeParser typeParser = new TypeParser();
        ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder(resourceRegistry,
            typeParser, objectMapper);
        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();
        return new RequestDispatcher(moduleRegistry, controllerRegistry, exceptionMapperRegistry);
    }
}
