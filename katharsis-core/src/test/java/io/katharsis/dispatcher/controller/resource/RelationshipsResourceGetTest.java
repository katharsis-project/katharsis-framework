package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.ReadContext;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.mock.models.ProjectPolymorphic;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.ProjectPolymorphicToObjectRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.ClassUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

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
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

        // THEN
        Assert.assertNotNull(response);
        String resultJson = objectMapper.writeValueAsString(response);
        assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
        assertThatJson(resultJson).node("data.type").isEqualTo("projects");
    }

    @Test
    public void supportPolymorphicRelationshipTypes() throws JsonProcessingException {
        // GIVEN
        Long projectId = 1L;
        String type = ClassUtils.getAnnotation(ProjectPolymorphic.class, JsonApiResource.class).get().type();

        ProjectPolymorphic projectPolymorphic = new ProjectPolymorphic();
        projectPolymorphic.setId(projectId);
        ProjectPolymorphicToObjectRepository projectPolymorphicToObjectRepository = new ProjectPolymorphicToObjectRepository();
        projectPolymorphicToObjectRepository.setRelation(projectPolymorphic, 42L, "task");

        JsonPath jsonPath = pathBuilder.buildPath("/" + type + "/" + projectId + "/relationships/task");
        RelationshipsResourceGet resourceGet = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext baseResponseContext = resourceGet.handle(jsonPath,
                new QueryParamsAdapter(REQUEST_PARAMS),
                null,
                null);
        // THEN
        Assert.assertNotNull(baseResponseContext);
        String resultJson = objectMapper.writeValueAsString(baseResponseContext);
        assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
        assertThatJson(resultJson).node("data.type").isEqualTo("tasks");

        // GIVEN
        projectPolymorphicToObjectRepository.setRelations(projectPolymorphic, Arrays.asList(44L, 45L), "tasks");
        jsonPath = pathBuilder.buildPath("/" + type + "/" + projectId + "/relationships/tasks");
        resourceGet = new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        baseResponseContext = resourceGet.handle(jsonPath,
                new QueryParamsAdapter(REQUEST_PARAMS),
                null,
                null);
        Assert.assertNotNull(baseResponseContext);

        resultJson = objectMapper.writeValueAsString(baseResponseContext);
        ReadContext resultCtx = com.jayway.jsonpath.JsonPath.parse(resultJson);
        assertInclude("45", 0, resultCtx);
        assertInclude("44", 1, resultCtx);

    }

    private void assertInclude(String id, int index, ReadContext resultCtx) {
        assertEquals("tasks", resultCtx.read("data[" + index + "].type"));
        String idStr = resultCtx.read("data[" + index + "].id").toString();
        assertEquals(id, idStr);

    }
}
