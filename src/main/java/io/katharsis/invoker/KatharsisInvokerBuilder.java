/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.katharsis.invoker;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.utils.parser.TypeParser;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * KatharsisInvoker builder.
 */
public class KatharsisInvokerBuilder {

    private ObjectMapper objectMapper;
    private QueryParamsBuilder queryParamsBuilder;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;
    private JsonServiceLocator jsonServiceLocator;
    private ExceptionMapperRegistry exceptionMapperRegistry;

    private String resourceSearchPackage;
    private String resourceDefaultDomain;

    public KatharsisInvokerBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public KatharsisInvokerBuilder queryParamsBuilder(QueryParamsBuilder queryParamsBuilder) {
        this.queryParamsBuilder = queryParamsBuilder;
        return this;
    }

    public KatharsisInvokerBuilder resourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
        return this;
    }

    public KatharsisInvokerBuilder requestDispatcher(RequestDispatcher requestDispatcher) {
        this.requestDispatcher = requestDispatcher;
        return this;
    }

    public KatharsisInvokerBuilder jsonServiceLocator(JsonServiceLocator jsonServiceLocator) {
        this.jsonServiceLocator = jsonServiceLocator;
        return this;
    }

    public KatharsisInvokerBuilder exceptionMapperRegistry(ExceptionMapperRegistry exceptionMapperRegistry) {
        this.exceptionMapperRegistry = exceptionMapperRegistry;
        return this;
    }

    public KatharsisInvokerBuilder resourceSearchPackage(String resourceSearchPackage) {
        this.resourceSearchPackage = resourceSearchPackage;
        return this;
    }

    public KatharsisInvokerBuilder resourceDefaultDomain(String resourceDefaultDomain) {
        this.resourceDefaultDomain = resourceDefaultDomain;
        return this;
    }

    public KatharsisInvoker build() throws Exception {
        if (resourceRegistry == null || requestDispatcher == null || exceptionMapperRegistry == null) {
            if (resourceSearchPackage == null) {
                throw new IllegalArgumentException("Resource Search Package should not be null.");
            }

            if (resourceDefaultDomain == null) {
                throw new IllegalArgumentException("Resource Default Domain should not be null.");
            }
        }

        if (resourceRegistry == null) {
            if (jsonServiceLocator == null) {
                throw new IllegalArgumentException("JsonServiceLocator should be provided!");
            }

            resourceRegistry = buildResourceRegistry(jsonServiceLocator, resourceSearchPackage, resourceDefaultDomain);
        }

        if (objectMapper == null) {
            objectMapper = createObjectMapper(resourceRegistry);
        }

        if (queryParamsBuilder == null) {
            queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        }

        if (requestDispatcher == null) {
            if (exceptionMapperRegistry == null) {
                exceptionMapperRegistry = buildExceptionMapperRegistry(resourceSearchPackage);
            }

            requestDispatcher = createRequestDispatcher(resourceRegistry, objectMapper, exceptionMapperRegistry);
        }

        return new KatharsisInvoker(objectMapper, queryParamsBuilder, resourceRegistry, requestDispatcher);
    }

    protected ExceptionMapperRegistry buildExceptionMapperRegistry(String resourceSearchPackage) throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(resourceSearchPackage);
    }


    protected ResourceRegistry buildResourceRegistry(JsonServiceLocator jsonServiceLocator, String resourceSearchPackage, String resourceDefaultDomain) {
        ResourceFieldNameTransformer resourceFieldNameTransformer;
        if (objectMapper != null) {
            resourceFieldNameTransformer =
                    new ResourceFieldNameTransformer(objectMapper.getSerializationConfig());
        } else {
            // As for now get default configuration if object mapper hasn't been initialized
            resourceFieldNameTransformer =
                    new ResourceFieldNameTransformer((new ObjectMapper()).getSerializationConfig());
        }
        ResourceRegistryBuilder registryBuilder =
                new ResourceRegistryBuilder(jsonServiceLocator, new ResourceInformationBuilder(resourceFieldNameTransformer));

        return registryBuilder.build(resourceSearchPackage, resourceDefaultDomain);
    }

    protected RequestDispatcher createRequestDispatcher(ResourceRegistry resourceRegistry,
                                                        ObjectMapper objectMapper,
                                                        ExceptionMapperRegistry exceptionMapperRegistry)
            throws Exception {

        TypeParser typeParser = new TypeParser();
        ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder(resourceRegistry, typeParser, objectMapper);
        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();

        return new RequestDispatcher(controllerRegistry, exceptionMapperRegistry);
    }

    protected ObjectMapper createObjectMapper(ResourceRegistry resourceRegistry) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(createDataBindingModule(resourceRegistry));
        return mapper;
    }

    protected Module createDataBindingModule(ResourceRegistry resourceRegistry) {
        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        SimpleModule simpleModule = jsonApiModuleBuilder.build(resourceRegistry);
        return simpleModule;
    }
}
