package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.TaskWithLookup;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.katharsis.request.path.JsonApiPath.parsePathFromStringUrl;
import static org.assertj.core.api.Assertions.assertThat;

public class ResourceGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";

    @Before
    public void before() {
        this.prepare();

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        DataBody data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        ObjectNode attributes = objectMapper.createObjectNode()
                .put("name", "sample project");
        attributes.putObject("data")
                .put("data", "asd");
        data.setAttributes(attributes);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);
    }

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/"), REQUEST_TYPE, null, parameterProvider);
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        Request request = new Request(parsePathFromStringUrl("http://domain.local/tasks/2"), REQUEST_TYPE, null, parameterProvider);
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter,
                queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");

        Request request = new Request(taskPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);
        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser,
                queryParamsBuilder, objectMapper);
        BaseResponseContext taskResponse = resourcePost.handle(request);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId);
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder, objectMapper);

        Request requestWithId = new Request(jsonPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);
        // WHEN
        BaseResponseContext response = sut.handle(requestWithId);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
        // GIVEN
        RequestBody body = new RequestBody(DataBody.builder().build());

//        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/task-with-lookup/1?include[task-with-lookup]=" +
//                "project,projectNull,projectOverridden,projectOverriddenNull");
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/task-with-lookup/1?include[task-with-lookup]=" +
                "project.projectNull.projectOverridden.projectOverriddenNull");

        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder, objectMapper);
        Request request = new Request(jsonPath, REQUEST_TYPE, serialize(body), parameterProvider);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(TaskWithLookup.class);
        TaskWithLookup responseData = (TaskWithLookup) (response.getResponse().getEntity());
        assertThat(responseData.getProject().getId()).isEqualTo(42L);
        //TODO: ieugen: un-comment when we support nested path include
//        assertThat(responseData.getProjectNull().getId()).isEqualTo(1L);
//        assertThat(responseData.getProjectOverridden().getId()).isEqualTo(1L);
//        assertThat(responseData.getProjectOverriddenNull().getId()).isEqualTo(1L);
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

        JsonApiPath path = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(path, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser,
                queryParamsBuilder, objectMapper);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(request);

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

        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        Request projectRequest = new Request(projectPath, REQUEST_TYPE, serialize(newProjectBody), parameterProvider);
        // WHEN -- adding a project
        BaseResponseContext projectResponse = resourcePost.handle(projectRequest);

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
        newTaskToProjectBody.setData(data);
        data.setType("projects");
        data.setId(projectId.toString());

        projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/relationships/project");
        request = new Request(projectPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);

        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser,
                queryParamsBuilder, objectMapper);

        // WHEN -- adding a relation between task and project
        BaseResponseContext projectRelationshipResponse = sut.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        //Given
        path = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId);
        request = new Request(path, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);

        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter
                , queryParamsBuilder, objectMapper);
        Map<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
                Collections.singleton("[project]"));
        QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (taskResponse.getResponse().getEntity())).getProject()).isNull();
    }

}
