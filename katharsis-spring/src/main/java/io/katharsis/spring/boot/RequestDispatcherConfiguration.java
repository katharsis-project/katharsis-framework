package io.katharsis.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.queryspec.internal.QueryAdapterBuilder;
import io.katharsis.queryspec.internal.QueryParamsAdapterBuilder;
import io.katharsis.queryspec.internal.QuerySpecAdapterBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

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
    
    @Autowired(required=false)
    private QueryParamsBuilder paramsBuilder;
    
    @Autowired(required=false)
    private QuerySpecDeserializer querySpecDeserializer;

    @Bean
    public RequestDispatcher requestDispatcher() throws Exception {
        TypeParser typeParser = new TypeParser();
        ControllerRegistryBuilder controllerRegistryBuilder =
            new ControllerRegistryBuilder(resourceRegistry, typeParser, objectMapper);
        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();
        
        QueryAdapterBuilder queryAdapterBuilder;
        if(querySpecDeserializer != null){
        	queryAdapterBuilder = new QuerySpecAdapterBuilder(querySpecDeserializer, resourceRegistry);
        }else{
        	queryAdapterBuilder = new QueryParamsAdapterBuilder(paramsBuilder, resourceRegistry);
        }
        
        return new RequestDispatcher(moduleRegistry, controllerRegistry, exceptionMapperRegistry, queryAdapterBuilder);
    }
}
