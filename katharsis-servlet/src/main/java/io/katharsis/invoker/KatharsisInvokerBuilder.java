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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.DefaultJsonApiDispatcher;
import io.katharsis.dispatcher.JsonApiDispatcher;
import io.katharsis.dispatcher.handlers.JsonApiDelete;
import io.katharsis.dispatcher.handlers.JsonApiGet;
import io.katharsis.dispatcher.handlers.JsonApiPatch;
import io.katharsis.dispatcher.handlers.JsonApiPost;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.RepositoryFactory;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;

/**
 * KatharsisInvoker builder.
 */
public class KatharsisInvokerBuilder {

    private ObjectMapper objectMapper;
    private RepositoryRegistry repositoryRegistry;
    private JsonApiDispatcher requestDispatcher;
    private RepositoryFactory jsonServiceLocator;
    private ExceptionMapperRegistry exceptionMapperRegistry;

    private String resourceSearchPackage;
    private String resourceDefaultDomain;
    private String apiPath;

    public KatharsisInvokerBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public KatharsisInvokerBuilder requestDispatcher(JsonApiDispatcher requestDispatcher) {
        this.requestDispatcher = requestDispatcher;
        return this;
    }

    public KatharsisInvokerBuilder repositoryFactory(RepositoryFactory jsonServiceLocator) {
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
        if (objectMapper == null) {
            objectMapper = createObjectMapper();
        }

        if (requestDispatcher == null) {
            if (exceptionMapperRegistry == null) {
                exceptionMapperRegistry = buildExceptionMapperRegistry(resourceSearchPackage);
            }

            requestDispatcher = createRequestDispatcher(repositoryRegistry, exceptionMapperRegistry);
        }

        return new KatharsisInvoker(objectMapper, requestDispatcher, apiPath);
    }

    protected ExceptionMapperRegistry buildExceptionMapperRegistry(String resourceSearchPackage) throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(resourceSearchPackage);
    }


    protected ResourceRegistry buildResourceRegistry(RepositoryFactory jsonServiceLocator, String resourceSearchPackage, String resourceDefaultDomain) {
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

    protected JsonApiDispatcher createRequestDispatcher(RepositoryRegistry repositoryRegistry,
                                                        ExceptionMapperRegistry exceptionMapperRegistry)
            throws Exception {

        return new DefaultJsonApiDispatcher(new JsonApiGet(repositoryRegistry),
                new JsonApiPost(repositoryRegistry),
                new JsonApiPatch(repositoryRegistry),
                new JsonApiDelete(repositoryRegistry),
                exceptionMapperRegistry);
    }

    protected ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(JsonApiModuleBuilder.create());
        return mapper;
    }

}
