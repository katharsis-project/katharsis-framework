package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.JsonApiDispatcher;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.spring.ErrorHandlerFilter;
import io.katharsis.spring.KatharsisFilterV2;
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
    private RepositoryRegistry repositoryRegistry;

    @Autowired
    private ExceptionMapperRegistry exceptionMapperRegistry;

    @Autowired
    private JsonApiDispatcher requestDispatcher;

    @Autowired
    private Module parameterNamesModule;

    @Bean
    public Filter springBootSampleKatharsisFilter() {
        objectMapper.registerModule(parameterNamesModule);
        return new KatharsisFilterV2(objectMapper, repositoryRegistry, requestDispatcher, properties.getPathPrefix());
    }

    @Bean
    public Filter errorHandlerFilter() {
        return new ErrorHandlerFilter(objectMapper, exceptionMapperRegistry);
    }
}
