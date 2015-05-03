package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourceGetTest extends BaseControllerTest {
    private static final String REQUEST_TYPE = "GET";

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestFieldResourceGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/project");
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestFieldResourcesGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), null);

        // THEN
        Assert.assertNotNull(response);
    }
}
