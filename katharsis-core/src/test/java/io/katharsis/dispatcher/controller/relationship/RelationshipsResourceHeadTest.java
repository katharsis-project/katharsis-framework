package io.katharsis.dispatcher.controller.relationship;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RelationshipsResourceHeadTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.HEAD.name();

    @Before
    public void prepareTest() throws Exception {
        TaskToProjectRepository localTaskToProjectRepository = new TaskToProjectRepository();
        localTaskToProjectRepository.removeRelations("project");
    }

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/relationships/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceHead sut = new RelationshipsResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onFieldRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks/1/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceHead sut = new RelationshipsResourceHead(resourceRegistry, typeParser, includeFieldSetter);

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
        RelationshipsResourceHead sut = new RelationshipsResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestLinkResourceHeadShouldReturnNullData() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/relationships/project");
        RelationshipsResourceHead sut = new RelationshipsResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, REQUEST_PARAMS, null, null);

        // THEN
        Assert.assertNull(response);
    }

    @Test
    public void onGivenRequestLinkResourcesHeadShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/users/1/relationships/assignedProjects");
        RelationshipsResourceHead sut = new RelationshipsResourceHead(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, REQUEST_PARAMS, null, null);

        // THEN
        Assert.assertNull(response);
    }
}
