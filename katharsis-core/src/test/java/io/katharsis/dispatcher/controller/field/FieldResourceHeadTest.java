package io.katharsis.dispatcher.controller.field;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourceHeadTest extends BaseControllerTest {
    private static final String REQUEST_TYPE = HttpMethod.HEAD.name();

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceHead sut = new FieldResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onRelationshipRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceHead sut = new FieldResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceHead sut = new FieldResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestFieldResourceHeadShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/project");
        FieldResourceHead sut = new FieldResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParams(), null, null);

        // THEN
        Assert.assertNull(response);
    }

    @Test
    public void onGivenRequestFieldResourcesHeadShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
        FieldResourceHead sut = new FieldResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParams(), null, null);

        // THEN
        Assert.assertNull(response);
    }
}
