package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourcePost;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourcePost;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";


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
    public void onGivenRequestCollectionGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParams(), null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestCollectionWithIdsGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1,2");
        CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParams(), null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestResourceWithIdShouldSetIt() throws Exception {
        // GIVEN
        RequestBody requestBody = new RequestBody();
        DataBody data = new DataBody();
        requestBody.setData(data);
        long taskId = Long.MAX_VALUE - 1L;
        data.setType("tasks");
        data.setId(Long.toString(taskId));

        JsonPath taskPath = pathBuilder.buildPath("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParams(), null, requestBody);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long persistedTaskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(persistedTaskId).isEqualTo(taskId);
    }

    @Test
    public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParams(), null, newTaskBody);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));

        JsonPath projectPath = pathBuilder.buildPath("/projects");

        // WHEN -- adding a project
        ResourceResponseContext projectResponse = resourcePost.handle(projectPath, new QueryParams(), null, newProjectBody);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
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
        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParams(), null,
            newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "includedProjects", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        //Given
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId );
        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        Map<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
            Collections.singleton("includedProjects"));
        QueryParams queryParams1 = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(jsonPath, queryParams1, null, null);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task)(taskResponse.getResponse().getEntity())).getIncludedProjects()).isNotNull();
        assertThat(((Task)(taskResponse.getResponse().getEntity())).getIncludedProjects().size()).isEqualTo(1);
        assertThat(((Task)(taskResponse.getResponse().getEntity())).getIncludedProjects().get(0).getId()).isEqualTo(projectId);
    }

    @Test
    public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParams(), null, newTaskBody);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));

        JsonPath projectPath = pathBuilder.buildPath("/projects");

        // WHEN -- adding a project
        ResourceResponseContext projectResponse = resourcePost.handle(projectPath, new QueryParams(), null, newProjectBody);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
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
        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParams(), null, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "projects", REQUEST_PARAMS);
        assertThat(project.getId()).isNotNull();

        //Given
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId );
        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        Map<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
            Collections.singleton("[\"projects\"]"));
        QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(jsonPath, requestParams, null, null);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task)(taskResponse.getResponse().getEntity())).getProjects()).isNull();
    }
}
