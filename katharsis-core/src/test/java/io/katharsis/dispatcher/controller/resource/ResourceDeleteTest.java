package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import org.junit.Ignore;
import org.junit.Test;

import static io.katharsis.request.path.JsonApiPath.parsePathFromStringUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ResourceDeleteTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "DELETE";

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/1"), REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ResourceDelete sut = new ResourceDelete(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    @Ignore
    ///TODO: ieugen: the logic has changed, we check if param name is relation lower in the code
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonApiPath path = parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project");
        Request request = new Request(path, REQUEST_TYPE, null, parameterProvider);
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ResourceDelete sut = new ResourceDelete(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
        // GIVEN

        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/1"), REQUEST_TYPE, null, parameterProvider);
        ResourceDelete sut = new ResourceDelete(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        assertThat(response).isNull();
    }
}
