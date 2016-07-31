package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.DefaultJsonApiDispatcher;
import io.katharsis.dispatcher.JsonApiDispatcher;
import io.katharsis.dispatcher.handlers.JsonApiDelete;
import io.katharsis.dispatcher.handlers.JsonApiGet;
import io.katharsis.dispatcher.handlers.JsonApiPatch;
import io.katharsis.dispatcher.handlers.JsonApiPost;
import io.katharsis.dispatcher.registry.DefaultRepositoryRegistry;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.errorhandling.mapper.DefaultExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.RepositoryFactory;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderLookup;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderRegistry;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderRegistryBuilder;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Basic Katharsis feature that initializes core classes and provides a starting point to use the framework in
 * another projects.
 * <p/>
 * This feature has NO {@link Provider} annotation, thus it require to provide an instance of  {@link ObjectMapper} and
 * {@link RepositoryFactory} to provide instances of resources.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class KatharsisFeature implements Feature {

    private final RepositoryFactory jsonServiceLocator;
    private final ObjectMapper objectMapper;

    public KatharsisFeature(ObjectMapper objectMapper,
                            QueryParamsBuilder queryParamsBuilder,
                            RepositoryFactory jsonServiceLocator) {
        this.objectMapper = objectMapper;
        this.jsonServiceLocator = jsonServiceLocator;
    }

    private static String buildServiceUrl(String resourceDefaultDomain, String webPathPrefix) {
        return resourceDefaultDomain + (webPathPrefix != null ? webPathPrefix : "");
    }

    private static ExceptionMapperRegistry buildExceptionMapperRegistry(ExceptionMapperLookup exceptionMapperLookup) throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(exceptionMapperLookup);
    }

    public ResourceLookup createResourceLookup(FeatureContext context) {
        String resourceSearchPackage = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);

        return new DefaultResourceLookup(resourceSearchPackage);
    }

    public ExceptionMapperLookup createExceptionMapperLookup(FeatureContext context) {
        String resourceSearchPackage = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);

        return new DefaultExceptionMapperLookup(resourceSearchPackage);
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

        String resourceSearchPackage = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);


        String serviceUrl = buildServiceUrl(resourceDefaultDomain, webPathPrefix);
        RepositoryRegistry repositoryRegistry = DefaultRepositoryRegistry.build(resourceSearchPackage, serviceUrl);

        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        objectMapper.registerModule(jsonApiModuleBuilder.create());

        KatharsisFilter katharsisFilter;
        try {
            ExceptionMapperLookup exceptionMapperLookup = createExceptionMapperLookup(context);
            ExceptionMapperRegistry exceptionMapperRegistry = buildExceptionMapperRegistry(exceptionMapperLookup);
            RequestContextParameterProviderLookup containerRequestContextProviderLookup = createRequestContextProviderLookup(context);
            RequestContextParameterProviderRegistry parameterProviderRegistry = buildParameterProviderRegistry(containerRequestContextProviderLookup);
            JsonApiDispatcher requestDispatcher = createRequestDispatcher(repositoryRegistry, exceptionMapperRegistry);

            katharsisFilter = createKatharsisFilter(parameterProviderRegistry, webPathPrefix, requestDispatcher);
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

    protected KatharsisFilter createKatharsisFilter(RequestContextParameterProviderRegistry parameterProviderRegistry,
                                                    String webPathPrefix,
                                                    JsonApiDispatcher requestDispatcher) throws Exception {

        return new KatharsisFilter(objectMapper, requestDispatcher, parameterProviderRegistry, webPathPrefix);
    }

    private JsonApiDispatcher createRequestDispatcher(RepositoryRegistry repositoryRegistry,
                                                      ExceptionMapperRegistry exceptionMapperRegistry) throws Exception {

        return new DefaultJsonApiDispatcher(new JsonApiGet(repositoryRegistry), new JsonApiPost(repositoryRegistry),
                new JsonApiPatch(repositoryRegistry), new JsonApiDelete(repositoryRegistry), exceptionMapperRegistry);
    }
}
