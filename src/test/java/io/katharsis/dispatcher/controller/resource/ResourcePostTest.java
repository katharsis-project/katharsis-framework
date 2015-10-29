package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.LinkageData;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.*;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.ResourceResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePostTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "POST";

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1");
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onInconsistentResourceTypesShouldThrowException() throws Exception {
        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        DataBody data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));

        JsonPath projectPath = pathBuilder.buildPath("/tasks");
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.handle(projectPath, new QueryParams(), null, newProjectBody);
    }

    @Test
    public void onNonExistentResourceShouldThrowException() throws Exception {
        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        DataBody data = new DataBody();
        newProjectBody.setData(data);
        data.setType("fridges");

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // THEN
        expectedException.expect(ResourceNotFoundException.class);

        // WHEN
        sut.handle(new ResourcePath("fridges"), new QueryParams(), null, newProjectBody);
    }

    @Test
    public void onNoBodyResourceShouldThrowException() throws Exception {
        // GIVEN
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.handle(new ResourcePath("fridges"), new QueryParams(), null, null);
    }

    @Test
    public void onNewResourcesAndRelationshipShouldPersistThoseData() throws Exception {
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

        JsonPath projectPath = pathBuilder.buildPath("/projects");
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN
        ResourceResponse projectResponse = sut.handle(projectPath, new QueryParams(), null, newProjectBody);

        // THEN
        assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(projectResponse.getData()).isExactlyInstanceOf(Project.class);
        Project persistedProject = (Project) (projectResponse.getData());
        assertThat(persistedProject.getId()).isNotNull();
        assertThat(persistedProject.getName()).isEqualTo("sample project");
        assertThat(persistedProject.getData()).isEqualToComparingFieldByField(new ProjectData().setData("asd"));
        Long projectId = ((Project) (projectResponse.getData())).getId();

        /* ------- */

        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());
        data.getRelationships().setAdditionalProperty("project", new LinkageData("projects", projectId.toString()));

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        // WHEN
        ResourceResponse taskResponse = sut.handle(taskPath, new QueryParams(), null, newTaskBody);

        // THEN
        assertThat(taskResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(taskResponse.getData()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getData())).getId();
        assertThat(taskId).isNotNull();
        assertThat(((Task) (taskResponse.getData())).getName()).isEqualTo("sample task");

        TaskRepository taskRepository = new TaskRepository();
        Task persistedTask = taskRepository.findOne(taskId, null);
        assertThat(persistedTask.getProject().getId()).isEqualTo(projectId);
    }

    @Test
    public void onNewResourcesAndRelationshipsShouldPersistThoseData() throws Exception {
        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        DataBody data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));

        JsonPath projectPath = pathBuilder.buildPath("/projects");
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN
        ResourceResponse projectResponse = sut.handle(projectPath, new QueryParams(), null, newProjectBody);

        // THEN
        assertThat(projectResponse.getData()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getData())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getData())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getData())).getId();

        /* ------- */

        // GIVEN
        RequestBody newUserBody = new RequestBody();
        data = new DataBody();
        newUserBody.setData(data);
        data.setType("users");
        data.setAttributes(objectMapper.createObjectNode().put("name", "some user"));
        data.setRelationships(new ResourceRelationships());
        data.getRelationships().setAdditionalProperty("assignedProjects", Collections.singletonList(new LinkageData("projects",
            projectId.toString())));

        JsonPath taskPath = pathBuilder.buildPath("/users");

        // WHEN
        ResourceResponse taskResponse = sut.handle(taskPath, new QueryParams(), null, newUserBody);

        // THEN
        assertThat(taskResponse.getData()).isExactlyInstanceOf(User.class);
        Long userId = ((User) (taskResponse.getData())).getId();
        assertThat(userId).isNotNull();
        assertThat(((User) (taskResponse.getData())).getName()).isEqualTo("some user");

        assertThat(((User) (taskResponse.getData())).getAssignedProjects()).hasSize(1);
        assertThat(((User) (taskResponse.getData())).getAssignedProjects().get(0).getId()).isEqualTo(projectId);
    }

    @Test
    public void onNewInheritedResourceShouldPersistThisResource() throws Exception {
        // GIVEN
        RequestBody newMemorandumBody = new RequestBody();
        DataBody data = new DataBody();
        newMemorandumBody.setData(data);
        data.setType("memoranda");
        ObjectNode attributes = objectMapper.createObjectNode()
            .put("title", "sample title")
            .put("body", "sample body");
        data.setAttributes(attributes);

        JsonPath projectPath = pathBuilder.buildPath("/documents");
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN
        ResourceResponse memorandumResponse = sut.handle(projectPath, new QueryParams(), null, newMemorandumBody);

        // THEN
        assertThat(memorandumResponse.getData()).isExactlyInstanceOf(Memorandum.class);
        Memorandum persistedMemorandum = (Memorandum) (memorandumResponse.getData());
        assertThat(persistedMemorandum.getId()).isNotNull();
        assertThat(persistedMemorandum.getTitle()).isEqualTo("sample title");
        assertThat(persistedMemorandum.getBody()).isEqualTo("sample body");
    }
}
