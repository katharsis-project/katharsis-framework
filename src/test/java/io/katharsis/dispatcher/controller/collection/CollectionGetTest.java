package io.katharsis.dispatcher.controller.collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourcePost;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourcePost;
import io.katharsis.queryParams.RequestParamsBuilder;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonPath;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.ResourceResponse;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";

    private static final RequestParams REQUEST_PARAMS = new RequestParams(OBJECT_MAPPER);

    @Test
    public void onGivenRequestCollectionGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/2");
        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestCollectionGetShouldHandleIt()
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestCollectionWithIdsGetShouldHandleIt()
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1,2");
        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER);

        // WHEN -- adding a task
        BaseResponse taskResponse = resourcePost.handle(taskPath, new RequestParams(new ObjectMapper()), newTaskBody);

        // THEN
        assertThat(taskResponse.getData()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getData())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample project"));

        JsonPath projectPath = pathBuilder.buildPath("/projects");

        // WHEN -- adding a project
        ResourceResponse projectResponse = resourcePost.handle(projectPath, new RequestParams(OBJECT_MAPPER), newProjectBody);

        // THEN
        assertThat(projectResponse.getData()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getData())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getData())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getData())).getId();
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody();
        data = new DataBody();
        newTaskToProjectBody.setData(Collections.singletonList(data));
        data.setType("projects");
        data.setId(projectId.toString());

        JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId + "/relationships/includedProjects");
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

        // WHEN -- adding a relation between task and project
        BaseResponse projectRelationshipResponse = sut.handle(savedTaskPath, new RequestParams(OBJECT_MAPPER), newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "includedProjects", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        //Given
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId );
        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name(), "[\"includedProjects\"]");
        RequestParams requestParams = new RequestParamsBuilder(new ObjectMapper()).buildRequestParams(queryParams);

        // WHEN
        BaseResponse<?> response = responseGetResp.handle(jsonPath, requestParams, null);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getData()).isExactlyInstanceOf(Task.class);
        assertThat(((Task)(taskResponse.getData())).getIncludedProjects()).isNotNull();
        assertThat(((Task)(taskResponse.getData())).getIncludedProjects().size()).isEqualTo(1);
        assertThat(((Task)(taskResponse.getData())).getIncludedProjects().get(0).getId()).isEqualTo(projectId);
    }

    @Test
    public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER);

        // WHEN -- adding a task
        BaseResponse taskResponse = resourcePost.handle(taskPath, new RequestParams(new ObjectMapper()), newTaskBody);

        // THEN
        assertThat(taskResponse.getData()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getData())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample project"));

        JsonPath projectPath = pathBuilder.buildPath("/projects");

        // WHEN -- adding a project
        ResourceResponse projectResponse = resourcePost.handle(projectPath, new RequestParams(OBJECT_MAPPER), newProjectBody);

        // THEN
        assertThat(projectResponse.getData()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getData())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getData())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getData())).getId();
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody();
        data = new DataBody();
        newTaskToProjectBody.setData(Collections.singletonList(data));
        data.setType("projects");
        data.setId(projectId.toString());

        JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId + "/relationships/projects");
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

        // WHEN -- adding a relation between task and project
        BaseResponse projectRelationshipResponse = sut.handle(savedTaskPath, new RequestParams(OBJECT_MAPPER), newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "projects", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        //Given
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId );
        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name(), "[\"projects\"]");
        RequestParams requestParams = new RequestParamsBuilder(new ObjectMapper()).buildRequestParams(queryParams);

        // WHEN
        BaseResponse<?> response = responseGetResp.handle(jsonPath, requestParams, null);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getData()).isExactlyInstanceOf(Task.class);
        assertThat(((Task)(taskResponse.getData())).getProjects()).isNull();
    }
}
