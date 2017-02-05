package io.katharsis.spring.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.boot.EmptyPropertiesProvider;
import io.katharsis.core.internal.dispatcher.ControllerRegistry;
import io.katharsis.core.internal.dispatcher.ControllerRegistryBuilder;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.query.QueryAdapterBuilder;
import io.katharsis.core.internal.query.QuerySpecAdapterBuilder;
import io.katharsis.core.internal.utils.parser.TypeParser;
import io.katharsis.legacy.internal.QueryParamsAdapterBuilder;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.resource.registry.ResourceRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestDispatcherConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModuleRegistry moduleRegistry;

    @Autowired
    private ResourceRegistry resourceRegistry;

    @Autowired
    private ExceptionMapperRegistry exceptionMapperRegistry;

    @Autowired(required = false)
    private QueryParamsBuilder paramsBuilder;

    @Autowired(required = false)
    private QuerySpecDeserializer querySpecDeserializer;

    @Bean
    public RequestDispatcher requestDispatcher() throws Exception {
        TypeParser typeParser = new TypeParser();
        ControllerRegistryBuilder controllerRegistryBuilder =
                new ControllerRegistryBuilder(resourceRegistry, typeParser, objectMapper, new EmptyPropertiesProvider());
        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();

        QueryAdapterBuilder queryAdapterBuilder;
        if (querySpecDeserializer != null) {
            queryAdapterBuilder = new QuerySpecAdapterBuilder(querySpecDeserializer, resourceRegistry);
        } else {
            queryAdapterBuilder = new QueryParamsAdapterBuilder(paramsBuilder, resourceRegistry);
        }

        return new RequestDispatcher(moduleRegistry, controllerRegistry, exceptionMapperRegistry, queryAdapterBuilder);
    }
}
