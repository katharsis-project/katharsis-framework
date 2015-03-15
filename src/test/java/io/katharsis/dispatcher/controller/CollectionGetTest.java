package io.katharsis.dispatcher.controller;

import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.ResourceRegistry;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class CollectionGetTest {

    @Test
    public void onGivenRequestCollectionGetShouldAcceptIt() {
        // GIVEN
        String path = "/resource/";
        String requestType = "GET";
        PathBuilder pathBuilder = new PathBuilder();
        ResourcePath resourcePath = pathBuilder.buildPath(path);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        CollectionGet sut = new CollectionGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(resourcePath, requestType);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestResourceGetShouldDenyIt() {
        // GIVEN
        String path = "/resource/2";
        String requestType = "GET";
        PathBuilder pathBuilder = new PathBuilder();
        ResourcePath resourcePath = pathBuilder.buildPath(path);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        CollectionGet sut = new CollectionGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(resourcePath, requestType);

        // THEN
        Assert.assertEquals(result, false);
    }
}
