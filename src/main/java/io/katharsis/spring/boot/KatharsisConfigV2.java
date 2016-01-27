package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.invoker.KatharsisInvokerBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.spring.KatharsisFilterV2;
import io.katharsis.spring.SpringServiceLocator;
import io.katharsis.utils.parser.TypeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.Filter;

@Configuration
@Import({RequestDispatcherConfiguration.class,
    JacksonConfiguration.class,
    JsonLocatorConfiguration.class,
    KatharsisRegistryConfiguration.class})
@EnableConfigurationProperties(KatharsisSpringBootProperties.class)
public class KatharsisConfigV2 {

    @Autowired
    private KatharsisSpringBootProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceRegistry resourceRegistry;

    @Autowired
    private RequestDispatcher requestDispatcher;

    @Bean
    public Filter springBootSampleKatharsisFilter() {
        return new KatharsisFilterV2(objectMapper, resourceRegistry, requestDispatcher, properties.getPathPrefix());
    }
}

@Configuration
class RequestDispatcherConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceRegistry resourceRegistry;

    @Autowired
    private ExceptionMapperRegistry exceptionMapperRegistry;

    @Bean
    public RequestDispatcher requestDispatcher() throws Exception {
        TypeParser typeParser = new TypeParser();
        ControllerRegistryBuilder controllerRegistryBuilder =
            new ControllerRegistryBuilder(resourceRegistry, typeParser, objectMapper);
        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();

        return new RequestDispatcher(controllerRegistry, exceptionMapperRegistry);
    }
}

@Configuration
class JacksonConfiguration {

    @Autowired
    private ResourceRegistry resourceRegistry;

    @Bean
    public Module parameterNamesModule() {
        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        return jsonApiModuleBuilder.build(resourceRegistry);
    }
}

@Configuration
class JsonLocatorConfiguration {

    @Bean
    public SpringServiceLocator serviceLocator() {
        return new SpringServiceLocator();
    }
}


@Configuration
@EnableConfigurationProperties(KatharsisSpringBootProperties.class)
class KatharsisRegistryConfiguration {

    @Autowired
    private KatharsisSpringBootProperties properties;

    @Autowired
    private SpringServiceLocator serviceLocator;

    @Bean
    public ResourceRegistry resourceRegistry() {
        ResourceRegistryBuilder registryBuilder =
            new ResourceRegistryBuilder(serviceLocator,
                new ResourceInformationBuilder(new ResourceFieldNameTransformer()));

        String serverUri = properties.getDomainName() + properties.getPathPrefix();
        return registryBuilder.build(properties.getResourcePackage(), serverUri);
    }

    @Bean
    public ExceptionMapperRegistry exceptionMapperRegistry() throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(properties.getResourcePackage());
    }
}