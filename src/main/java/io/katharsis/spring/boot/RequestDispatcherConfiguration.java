package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestDispatcherConfiguration {

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
