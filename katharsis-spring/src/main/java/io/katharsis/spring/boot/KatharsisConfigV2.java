package io.katharsis.spring.boot;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.spring.ErrorHandlerFilter;
import io.katharsis.spring.KatharsisFilterV2;

@Configuration
@Import({RequestDispatcherConfiguration.class,
        QueryParamsBuilderConfiguration.class,
    JacksonConfiguration.class,
    JsonLocatorConfiguration.class,
    ModuleConfiguration.class,
    KatharsisRegistryConfiguration.class})
@EnableConfigurationProperties(KatharsisSpringBootProperties.class)
/**
 * @Deprecated in favor of new version with JSON API compliance, QuerySpec and module support. 
 */
@Deprecated
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
    private Module parameterNamesModule;

    @Bean
    public Filter springBootSampleKatharsisFilter() {
        objectMapper.registerModule(parameterNamesModule);
        return new KatharsisFilterV2(objectMapper, resourceRegistry, requestDispatcher,
                properties);
    }

    @Bean
    public Filter errorHandlerFilter() {
        return new ErrorHandlerFilter(objectMapper, exceptionMapperRegistry);
    }
}
