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
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryParams.QueryParamsParser;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.spring.ErrorHandlerFilter;
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
        QueryParamsBuilderConfiguration.class,
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
    private ExceptionMapperRegistry exceptionMapperRegistry;

    @Autowired
    private RequestDispatcher requestDispatcher;

    @Autowired
    private QueryParamsBuilder queryParamsBuilder;

    @Autowired
    private Module parameterNamesModule;

    @Bean
    public Filter springBootSampleKatharsisFilter() {
        objectMapper.registerModule(parameterNamesModule);
        return new KatharsisFilterV2(objectMapper, queryParamsBuilder, resourceRegistry, requestDispatcher,
                properties.getPathPrefix());
    }

    @Bean
    public Filter errorHandlerFilter() {
        return new ErrorHandlerFilter(objectMapper, exceptionMapperRegistry);
    }
}
