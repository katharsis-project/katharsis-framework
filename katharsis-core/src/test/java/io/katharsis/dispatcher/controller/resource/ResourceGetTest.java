package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.TaskWithLookup;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
    }

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/");
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/2");
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

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

        JsonPath taskPath = pathBuilder.build("/tasks");

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);
        ResourceResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

        // THEN
        Assert.assertNotNull(response);
    }


    @Test
    public void onGivenNonExistingRequestResourceGetShouldReturnErrorResponse() throws Exception {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/1");
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

        // THEN
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getHttpStatus());
        Assert.assertNotNull(response.getResponse().getEntity());
    }

    @Test
    public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/task-with-lookup/1");
        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        Map<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[task-with-lookup]",
                new HashSet<>(Arrays.asList("project", "projectNull", "projectOverridden", "projectOverriddenNull")));
        QueryParams queryParamsObject = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(queryParamsObject), null, null);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(TaskWithLookup.class);
        TaskWithLookup responseData = (TaskWithLookup) (response.getResponse().getEntity());
        assertThat(responseData.getProject().getId()).isEqualTo(42L);
        assertThat(responseData.getProjectNull().getId()).isEqualTo(1L);
        assertThat(responseData.getProjectOverridden().getId()).isEqualTo(1L);
        assertThat(responseData.getProjectOverriddenNull().getId()).isEqualTo(1L);
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

        JsonPath taskPath = pathBuilder.build("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);

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

        JsonPath projectPath = pathBuilder.build("/projects");

        // WHEN -- adding a project
        ResourceResponseContext projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

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

        JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/project");
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

        // WHEN -- adding a relation between task and project
        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);

        //Given
        JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
        ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        Map<String, Set<String>> queryParams = new HashMap<>();
        queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]",
                Collections.singleton("[\"project\"]"));
        QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

        // WHEN
        BaseResponseContext response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(requestParams), null, null);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (taskResponse.getResponse().getEntity())).getProject()).isNull();
    }

}
