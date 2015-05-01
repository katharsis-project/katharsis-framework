package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.path.JsonPath;
import io.katharsis.path.ResourcePath;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ResourceDeleteTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "DELETE";

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ResourceDelete sut = new ResourceDelete(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks/1/links/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ResourceDelete sut = new ResourceDelete(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1");
        ResourceDelete sut = new ResourceDelete(resourceRegistry);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), null);

        // THEN
        assertThat(response).isNull();
    }
}
