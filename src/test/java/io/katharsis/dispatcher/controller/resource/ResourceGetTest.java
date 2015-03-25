package io.katharsis.dispatcher.controller.resource;

import io.katharsis.context.SampleJsonApplicationContext;
import io.katharsis.path.JsonPath;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.path.PathBuilder;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.BaseResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResourceGetTest {

    private PathBuilder pathBuilder;
    private String requestType;
    private ResourceRegistry resourceRegistry;

    @Before
    public void prepare() {
        pathBuilder = new PathBuilder();
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonApplicationContext(), new ResourceInformationBuilder());
        resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
        requestType = "GET";
    }

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/resource/");
        ResourceGet sut = new ResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, requestType);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/resource/2");
        ResourceGet sut = new ResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, requestType);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1");
        ResourceGet sut = new ResourceGet(resourceRegistry);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath);

        // THEN
        Assert.assertNotNull(response);
    }
}
