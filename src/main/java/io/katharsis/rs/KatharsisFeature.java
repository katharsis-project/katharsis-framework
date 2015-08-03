package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
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
 *
 * This feature has NO {@link Provider} annotation, thus it require to provide an instance of  {@link ObjectMapper} and
 * {@link JsonServiceLocator} to provide instances of resources.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class KatharsisFeature implements Feature {

    private final JsonServiceLocator jsonServiceLocator;
    private final ObjectMapper objectMapper;

    public KatharsisFeature(ObjectMapper objectMapper, JsonServiceLocator jsonServiceLocator) {
        this.objectMapper = objectMapper;
        this.jsonServiceLocator = jsonServiceLocator;
    }

    @Override
    public boolean configure(FeatureContext context) {
        String resourceSearchPackage = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);
        String resourceDefaultDomain = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN);
        String webPathPrefix = (String) context
                .getConfiguration()
                .getProperty(KatharsisProperties.WEB_PATH_PREFIX);

        String serviceUrl = buildServiceUrl(resourceDefaultDomain, webPathPrefix);
        ResourceRegistry resourceRegistry = buildResourceRegistry(resourceSearchPackage, serviceUrl);

        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        objectMapper.registerModule(jsonApiModuleBuilder.build(resourceRegistry));

        KatharsisFilter katharsisFilter;
        try {
            ExceptionMapperRegistry exceptionMapperRegistry = buildExceptionMapperRegistry(resourceSearchPackage);
            katharsisFilter = createKatharsisFilter(resourceRegistry, exceptionMapperRegistry, webPathPrefix);
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
        context.register(katharsisFilter);

        return true;
    }

    private String buildServiceUrl(String resourceDefaultDomain, String webPathPrefix) {
        return resourceDefaultDomain + (webPathPrefix != null ? webPathPrefix : "");
    }

    private ExceptionMapperRegistry buildExceptionMapperRegistry(String resourceSearchPackage) throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(resourceSearchPackage);
    }

    private ResourceRegistry buildResourceRegistry(String resourceSearchPackage, String serviceUrl) {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(jsonServiceLocator, new ResourceInformationBuilder());
        return registryBuilder.build(resourceSearchPackage, serviceUrl);
    }

    private KatharsisFilter createKatharsisFilter(ResourceRegistry resourceRegistry,
        ExceptionMapperRegistry exceptionMapperRegistry, String webPathPrefix) throws Exception {
        RequestDispatcher requestDispatcher = createRequestDispatcher(resourceRegistry, exceptionMapperRegistry);

        return new KatharsisFilter(objectMapper, resourceRegistry, requestDispatcher, webPathPrefix);
    }

    private RequestDispatcher createRequestDispatcher(ResourceRegistry resourceRegistry, ExceptionMapperRegistry exceptionMapperRegistry) throws Exception {
        ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder();
        TypeParser typeParser = new TypeParser();
        ControllerRegistry controllerRegistry = controllerRegistryBuilder
                .build(resourceRegistry, typeParser);
        return new RequestDispatcher(controllerRegistry, exceptionMapperRegistry);
    }
}
