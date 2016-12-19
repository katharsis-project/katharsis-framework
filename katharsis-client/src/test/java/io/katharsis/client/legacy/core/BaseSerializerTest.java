package io.katharsis.client.legacy.core;

import static io.katharsis.resource.registry.ResourceRegistryTest.TEST_MODELS_URL;

import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.internal.core.ResourceResponseContext;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.response.JsonApiResponse;

public abstract class BaseSerializerTest {

    ObjectMapper sut;
    protected ResourceRegistry resourceRegistry;

    protected ResourceResponseContext testResponse;

    @Before
    public void setUp() throws Exception {
        ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
            new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
            resourceInformationBuilder);
        resourceRegistry = registryBuilder
            .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(TEST_MODELS_URL));

        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();

        sut = new ObjectMapper();
        sut.registerModule(jsonApiModuleBuilder.build(resourceRegistry, false));

        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");
        testResponse = new ResourceResponseContext(buildResponse(null), jsonPath, new QueryParamsAdapter(new QueryParams()));
    }

    protected JsonApiResponse buildResponse(Object resource) {
        return new JsonApiResponse()
            .setEntity(resource);
    }
}
