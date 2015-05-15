package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorHandling.ExceptionMapperRegistry;
import io.katharsis.errorHandling.ExceptionMapperRegistryBuilder;
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

        ResourceRegistry resourceRegistry = buildResourceRegistry(resourceSearchPackage, resourceDefaultDomain);
        ExceptionMapperRegistry exceptionMapperRegistry = buildExceptionMapperRegistry(resourceSearchPackage);

        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        objectMapper.registerModule(jsonApiModuleBuilder.build(resourceRegistry));

        KatharsisFilter katharsisFilter;
        try {
            katharsisFilter = createKatharsisFilter(resourceRegistry, exceptionMapperRegistry);
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
        context.register(katharsisFilter);

        return true;
    }

    private ExceptionMapperRegistry buildExceptionMapperRegistry(String resourceSearchPackage) {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(resourceSearchPackage);
    }

    private ResourceRegistry buildResourceRegistry(String resourceSearchPackage, String resourceDefaultDomain) {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(jsonServiceLocator, new ResourceInformationBuilder());
        return registryBuilder.build(resourceSearchPackage, resourceDefaultDomain);
    }

    private KatharsisFilter createKatharsisFilter(ResourceRegistry resourceRegistry, ExceptionMapperRegistry exceptionMapperRegistry) throws Exception {
        RequestDispatcher requestDispatcher = createRequestDispatcher(resourceRegistry, exceptionMapperRegistry);

        return new KatharsisFilter(objectMapper, resourceRegistry, requestDispatcher);
    }

    private RequestDispatcher createRequestDispatcher(ResourceRegistry resourceRegistry, ExceptionMapperRegistry exceptionMapperRegistry) throws Exception {
        ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder();
        TypeParser typeParser = new TypeParser();
        ControllerRegistry controllerRegistry = controllerRegistryBuilder
                .build(resourceRegistry, typeParser);
        return new RequestDispatcher(controllerRegistry, exceptionMapperRegistry);
    }
}
