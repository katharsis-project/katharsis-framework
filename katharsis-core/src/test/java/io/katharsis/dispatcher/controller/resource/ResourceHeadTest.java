package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.request.path.JsonPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ResourceHeadTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.HEAD.name();

    @Before
    public void before() {
        this.prepare();
    }

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        ResourceHead sut = new ResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceHeadShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/2");
        ResourceHead sut = new ResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }
}
