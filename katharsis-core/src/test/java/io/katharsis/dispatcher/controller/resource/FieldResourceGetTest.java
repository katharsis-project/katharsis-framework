package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.Request;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Test;

import static io.katharsis.request.path.JsonApiPath.parsePathFromStringUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourceGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/1/project"), REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onRelationshipRequestShouldDenyIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project"), REQUEST_TYPE, null, parameterProvider);
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks"), REQUEST_TYPE, null, parameterProvider);
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter
                , queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestFieldResourceGetShouldHandleIt() throws Exception {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/1/project"), REQUEST_TYPE, null, parameterProvider);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestFieldResourcesGetShouldHandleIt() throws Exception {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/users/1/assignedProjects"), REQUEST_TYPE, null, parameterProvider);

        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
    }
}
