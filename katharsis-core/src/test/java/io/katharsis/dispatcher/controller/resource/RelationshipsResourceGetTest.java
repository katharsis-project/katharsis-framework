package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
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
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser,
                includeFieldSetter, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isTrue();
    }


    @Test
    public void onFieldRequestShouldDenyIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1/project");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser,
                includeFieldSetter, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestLinkResourceGetShouldReturnNullData() throws Exception {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestLinkResourceGetShouldReturnDataField() throws Exception {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);
        new TaskToProjectRepository().setRelation(new Task().setId(1L), 42L, "project");

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
        String resultJson = objectMapper.writeValueAsString(response);
        assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
        assertThatJson(resultJson).node("data.type").isEqualTo("projects");
    }

    @Test
    public void onGivenRequestLinkResourcesGetShouldHandleIt() throws Exception {
        // GIVEN

        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/users/1/relationships/assignedProjects");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);
        RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.OK_200);
    }
}
