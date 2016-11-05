package io.katharsis.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.CoreModule;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryAdapterBuilder;
import io.katharsis.queryspec.internal.QueryParamsAdapterBuilder;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.utils.parser.TypeParser;
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
                new ControllerRegistryBuilder(resourceRegistry, typeParser, objectMapper);

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
