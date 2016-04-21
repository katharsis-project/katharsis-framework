package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseControllerTest;
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

public class RelationshipsResourceGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";
    private TaskToProjectRepository localTaskToProjectRepository;

    @Before
    public void prepareTest() throws Exception {
        localTaskToProjectRepository = new TaskToProjectRepository();
        localTaskToProjectRepository.removeRelations("project");
    }

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/relationships/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);

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
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);

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
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestLinkResourceGetShouldReturnNullData() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/relationships/project");
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, REQUEST_PARAMS, null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestLinkResourceGetShouldReturnDataField() throws Exception {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/relationships/project");
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        new TaskToProjectRepository().setRelation(new Task().setId(1L), 42L, "project");

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, REQUEST_PARAMS, null, null);

        // THEN
        Assert.assertNotNull(response);
        String resultJson = objectMapper.writeValueAsString(response);
        assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
        assertThatJson(resultJson).node("data.type").isEqualTo("projects");
    }

    @Test
    public void onGivenRequestLinkResourcesGetShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/users/1/relationships/assignedProjects");
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, REQUEST_PARAMS, null, null);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK_200);
    }
}
