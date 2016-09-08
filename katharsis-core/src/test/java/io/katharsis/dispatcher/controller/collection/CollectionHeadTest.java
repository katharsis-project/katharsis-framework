package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Test;


public class CollectionHeadTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.HEAD.name();


    @Test
    public void onGivenRequestCollectionHeadShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        CollectionHead sut = new CollectionHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestCollectionHeadShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/2");
        CollectionHead sut = new CollectionHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestCollectionHeadShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        CollectionHead sut = new CollectionHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParams(), null, null);

        // THEN
        Assert.assertNull(response);
    }

    @Test
    public void onGivenRequestCollectionWithIdsHeadShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1,2");
        CollectionHead sut = new CollectionHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParams(), null, null);

        // THEN
        Assert.assertNull(response);
    }
}
