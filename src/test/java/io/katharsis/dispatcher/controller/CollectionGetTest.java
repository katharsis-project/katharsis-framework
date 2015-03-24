package io.katharsis.dispatcher.controller;

import io.katharsis.context.SampleJsonApplicationContext;
import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.BaseResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CollectionGetTest {

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
    public void onGivenRequestCollectionGetShouldAcceptIt() {
        // GIVEN
        ResourcePath resourcePath = pathBuilder.buildPath("/resource/");
        CollectionGet sut = new CollectionGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(resourcePath, requestType);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        ResourcePath resourcePath = pathBuilder.buildPath("/resource/2");
        CollectionGet sut = new CollectionGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(resourcePath, requestType);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestCollectionGetShouldHandleIt() {
        // GIVEN

        ResourcePath resourcePath = pathBuilder.buildPath("/tasks/");
        CollectionGet sut = new CollectionGet(resourceRegistry);

        // WHEN
        BaseResponse<?> response = sut.handle(resourcePath);

        // THEN
        Assert.assertNotNull(response);
    }
}
