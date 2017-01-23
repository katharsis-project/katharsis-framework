package io.katharsis.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import io.katharsis.core.internal.boot.EmptyPropertiesProvider;
import io.katharsis.core.internal.dispatcher.ControllerRegistry;
import io.katharsis.core.internal.dispatcher.ControllerRegistryBuilder;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.exception.ExceptionMapperRegistryBuilder;
import io.katharsis.core.internal.jackson.JsonApiModuleBuilder;
import io.katharsis.core.internal.query.QueryAdapterBuilder;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.core.internal.utils.parser.TypeParser;
import io.katharsis.legacy.internal.QueryParamsAdapterBuilder;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.CoreModule;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.vertx.core.json.Json;
import lombok.NonNull;

public class KatharsisHandlerFactory {

    public static KatharsisHandler create(@NonNull String packagesToScan,
                                          @NonNull String webPath) {

        return create(packagesToScan, webPath, Json.mapper);
    }

    public static KatharsisHandler create(@NonNull String packagesToScan,
                                          @NonNull String webPath,
                                          @NonNull ObjectMapper objectMapper) {

        return create(packagesToScan, webPath, objectMapper, new DefaultParameterProviderFactory());
    }

    public static KatharsisHandler create(@NonNull String packagesToScan,
                                          @NonNull String webPath,
                                          @NonNull ObjectMapper objectMapper,
                                          @NonNull ParameterProviderFactory parameterProviderFactory) {

        ExceptionMapperRegistry exceptionMapperRegistry = buildExceptionMapperRegistry(packagesToScan);
        ModuleRegistry moduleRegistry = buildModuleRegistry(objectMapper, packagesToScan);
        ResourceRegistry resourceRegistry = buildRegistry(packagesToScan, webPath, moduleRegistry);


        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        objectMapper.registerModule(jsonApiModuleBuilder.build(resourceRegistry, false));

        RequestDispatcher requestDispatcher = createRequestDispatcher(objectMapper, moduleRegistry,
                exceptionMapperRegistry, resourceRegistry);

        PathBuilder pathBuilder = new PathBuilder(resourceRegistry);

        return new KatharsisHandler(objectMapper, webPath,
                pathBuilder, parameterProviderFactory, requestDispatcher);
    }

    private static RequestDispatcher createRequestDispatcher(@NonNull ObjectMapper objectMapper,
                                                             @NonNull ModuleRegistry moduleRegistry,
                                                             @NonNull ExceptionMapperRegistry exceptionMapperRegistry,
                                                             @NonNull ResourceRegistry resourceRegistry) {
        TypeParser typeParser = new TypeParser();
        ControllerRegistryBuilder controllerRegistryBuilder =
                new ControllerRegistryBuilder(resourceRegistry, typeParser, objectMapper, new EmptyPropertiesProvider());

        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();

        // TODO QuerySpec processing support
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryAdapterBuilder queryAdapterBuilder = new QueryParamsAdapterBuilder(queryParamsBuilder, resourceRegistry);

        return new RequestDispatcher(moduleRegistry, controllerRegistry, exceptionMapperRegistry, queryAdapterBuilder);
    }

    public static ModuleRegistry buildModuleRegistry(@NonNull ObjectMapper objectMapper,
                                                     @NonNull String packageToScan) {
        ModuleRegistry moduleRegistry = new ModuleRegistry();
        ResourceFieldNameTransformer resourceFieldNameTransformer = new ResourceFieldNameTransformer(
                objectMapper.getSerializationConfig());
        ModuleRegistry registry = new ModuleRegistry();
        registry.addModule(new CoreModule(packageToScan, resourceFieldNameTransformer));
        return moduleRegistry;
    }

    public static ResourceRegistry buildRegistry(@NonNull String packageToScan, @NonNull String webPath, @NonNull ModuleRegistry moduleRegistry) {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(
                new SampleJsonServiceLocator(),
                new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer()));

        ServiceUrlProvider serviceUrlProvider = new ConstantServiceUrlProvider(webPath);
        return registryBuilder.build(packageToScan, moduleRegistry, serviceUrlProvider);
    }

    private static ExceptionMapperRegistry buildExceptionMapperRegistry(String resourceSearchPackage) {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        try {
            return mapperRegistryBuilder.build(resourceSearchPackage);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
