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
package io.katharsis.invoker.internal.legacy;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.boot.PropertiesProvider;
import io.katharsis.core.internal.dispatcher.ControllerRegistry;
import io.katharsis.core.internal.dispatcher.ControllerRegistryBuilder;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.exception.ExceptionMapperRegistryBuilder;
import io.katharsis.core.internal.jackson.JsonApiModuleBuilder;
import io.katharsis.core.internal.query.QueryAdapterBuilder;
import io.katharsis.core.internal.query.QuerySpecAdapterBuilder;
import io.katharsis.legacy.internal.QueryParamsAdapterBuilder;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.CoreModule;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

/**
 * KatharsisInvoker builder.
 */
@Deprecated
public class KatharsisInvokerBuilder {

    private ModuleRegistry moduleRegistry = new ModuleRegistry();

    private ObjectMapper objectMapper;
    private QueryParamsBuilder queryParamsBuilder;
    private QuerySpecDeserializer querySpecDeserializer;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;
    private JsonServiceLocator jsonServiceLocator;
    private ExceptionMapperRegistry exceptionMapperRegistry;
    private PropertiesProvider propertiesProvider;

    private String resourceSearchPackage;
    private String resourceDefaultDomain;


    public KatharsisInvokerBuilder module(io.katharsis.module.Module module) {
        moduleRegistry.addModule(module);
        return this;
    }

    public KatharsisInvokerBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public KatharsisInvokerBuilder propertiesProvider(PropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        return this;
    }

    public KatharsisInvokerBuilder queryParamsBuilder(QueryParamsBuilder queryParamsBuilder) {
        this.queryParamsBuilder = queryParamsBuilder;
        return this;
    }

    public KatharsisInvokerBuilder querySpecDeserializer(QuerySpecDeserializer querySpecDeserializer) {
        this.querySpecDeserializer = querySpecDeserializer;
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

        ResourceFieldNameTransformer buildResourceFieldNameTransformer = buildResourceFieldNameTransformer();
        moduleRegistry.addModule(new CoreModule(resourceSearchPackage, buildResourceFieldNameTransformer));

        if (resourceRegistry == null) {
            if (jsonServiceLocator == null) {
                throw new IllegalArgumentException("JsonServiceLocator should be provided!");
            }

            resourceRegistry = buildResourceRegistry(jsonServiceLocator, resourceSearchPackage, resourceDefaultDomain);
        }

        if (objectMapper == null) {
            objectMapper = createObjectMapper(resourceRegistry);
        }

        moduleRegistry.init(objectMapper);

        if (requestDispatcher == null) {
            if (exceptionMapperRegistry == null) {
                exceptionMapperRegistry = buildExceptionMapperRegistry(resourceSearchPackage);
            }

            requestDispatcher = createRequestDispatcher(moduleRegistry, objectMapper, exceptionMapperRegistry);
        }

        return new KatharsisInvoker(objectMapper, resourceRegistry, requestDispatcher, propertiesProvider);
    }

    protected ResourceFieldNameTransformer buildResourceFieldNameTransformer() {
        ResourceFieldNameTransformer resourceFieldNameTransformer;
        if (objectMapper != null) {
            resourceFieldNameTransformer =
                    new ResourceFieldNameTransformer(objectMapper.getSerializationConfig());
        } else {
            // As for now get default configuration if object mapper hasn't been initialized
            resourceFieldNameTransformer =
                    new ResourceFieldNameTransformer((new ObjectMapper()).getSerializationConfig());
        }
        return resourceFieldNameTransformer;
    }

    protected ExceptionMapperRegistry buildExceptionMapperRegistry(String resourceSearchPackage) throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(resourceSearchPackage);
    }


    protected ResourceRegistry buildResourceRegistry(JsonServiceLocator jsonServiceLocator, String resourceSearchPackage, String resourceDefaultDomain) {
        ResourceRegistryBuilder registryBuilder =
                new ResourceRegistryBuilder(moduleRegistry, jsonServiceLocator, moduleRegistry.getResourceInformationBuilder());

        return registryBuilder.build(resourceSearchPackage, moduleRegistry, new ConstantServiceUrlProvider(resourceDefaultDomain));
    }

    protected RequestDispatcher createRequestDispatcher(ModuleRegistry moduleRegistry,
                                                        ObjectMapper objectMapper,
                                                        ExceptionMapperRegistry exceptionMapperRegistry) throws Exception {
        TypeParser typeParser = moduleRegistry.getTypeParser();
        ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder(resourceRegistry, typeParser, objectMapper, propertiesProvider);
        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();

        QueryAdapterBuilder queryAdapterBuilder;
        if (querySpecDeserializer != null) {
            queryAdapterBuilder = new QuerySpecAdapterBuilder(querySpecDeserializer, moduleRegistry);
        } else {
            if (queryParamsBuilder == null) {
                queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
            }
            queryAdapterBuilder = new QueryParamsAdapterBuilder(queryParamsBuilder, resourceRegistry);
        }

        return new RequestDispatcher(moduleRegistry, controllerRegistry, exceptionMapperRegistry, queryAdapterBuilder);
    }

    protected ObjectMapper createObjectMapper(ResourceRegistry resourceRegistry) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(createDataBindingModule(resourceRegistry));
        return mapper;
    }

    protected Module createDataBindingModule(ResourceRegistry resourceRegistry) {
        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        return jsonApiModuleBuilder.build(resourceRegistry, false);
    }
}
