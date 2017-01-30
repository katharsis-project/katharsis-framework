package io.katharsis.core.internal.dispatcher.controller.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import io.katharsis.core.internal.dispatcher.controller.BaseControllerTest;
import io.katharsis.core.internal.dispatcher.controller.ResourceDelete;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.ResourcePath;
import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.registry.ResourceRegistry;

public class ResourceDeleteTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "DELETE";

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ResourceDelete sut = new ResourceDelete(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ResourceDelete sut = new ResourceDelete(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1");
        ResourceDelete sut = new ResourceDelete(resourceRegistry, typeParser);

        // WHEN
        Response response = sut.handle(jsonPath, new QueryParamsAdapter(new QueryParams()), null, null);

        // THEN
        assertThat(response.getDocument()).isNull();
    }
}
