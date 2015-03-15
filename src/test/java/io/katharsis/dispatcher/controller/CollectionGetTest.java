package io.katharsis.dispatcher.controller;

import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.ResourceRegistry;
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
        resourceRegistry = new ResourceRegistry();
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
    public void onGivenRequestResourceGetShouldDenyIt() {
        // GIVEN
        ResourcePath resourcePath = pathBuilder.buildPath("/resource/2");
        CollectionGet sut = new CollectionGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(resourcePath, requestType);

        // THEN
        Assert.assertEquals(result, false);
    }
}
