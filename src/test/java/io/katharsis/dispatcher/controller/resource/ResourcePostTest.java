package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.Linkage;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceLinks;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePostTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "POST";

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1");
        ResourcePost sut = new ResourcePost(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        ResourcePost sut = new ResourcePost(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onInconsistentResourceTypesShouldThrowException() throws Exception {
        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        newProjectBody.setData(new DataBody());
        newProjectBody.getData().setType("projects");
        newProjectBody.getData().setAdditionalProperty("name", "sample project");

        JsonPath projectPath = pathBuilder.buildPath("/tasks");
        ResourcePost sut = new ResourcePost(resourceRegistry);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.handle(projectPath, new RequestParams(new ObjectMapper()), newProjectBody);
    }

    @Test
    public void onNonExistentResourceShouldThrowException() throws Exception {
        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        newProjectBody.setData(new DataBody());
        newProjectBody.getData().setType("fridges");

        ResourcePost sut = new ResourcePost(resourceRegistry);

        // THEN
        expectedException.expect(ResourceNotFoundException.class);

        // WHEN
        sut.handle(new ResourcePath("fridges"), new RequestParams(new ObjectMapper()), newProjectBody);
    }

    @Test
    public void onNewResourcesAndRelationshipShouldPersistThoseData() throws Exception {
        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        newProjectBody.setData(new DataBody());
        newProjectBody.getData().setType("projects");
        newProjectBody.getData().setAdditionalProperty("name", "sample project");

        JsonPath projectPath = pathBuilder.buildPath("/projects");
        ResourcePost sut = new ResourcePost(resourceRegistry);

        // WHEN
        ResourceResponse projectResponse = sut.handle(projectPath, new RequestParams(new ObjectMapper()), newProjectBody);

        // THEN
        assertThat(projectResponse.getData()).isExactlyInstanceOf(Container.class);
        assertThat(((Container) projectResponse.getData()).getData()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (((Container) projectResponse.getData()).getData())).getId()).isNotNull();
        assertThat(((Project) (((Container) projectResponse.getData()).getData())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (((Container) projectResponse.getData()).getData())).getId();

        /* ------- */

        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        newTaskBody.setData(new DataBody());
        newTaskBody.getData().setType("tasks");
        newTaskBody.getData().setAdditionalProperty("name", "sample task");
        newTaskBody.getData().setLinks(new ResourceLinks());
        newTaskBody.getData().getLinks().setAdditionalProperty("project", new Linkage("projects", projectId.toString()));

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        // WHEN
        ResourceResponse taskResponse = sut.handle(taskPath, new RequestParams(new ObjectMapper()), newTaskBody);

        // THEN
        assertThat(taskResponse.getData()).isExactlyInstanceOf(Container.class);
        assertThat(((Container) taskResponse.getData()).getData()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (((Container) taskResponse.getData()).getData())).getId()).isNotNull();
        assertThat(((Task) (((Container) taskResponse.getData()).getData())).getName()).isEqualTo("sample task");

        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(projectId, "project");
        assertThat(project.getId()).isEqualTo(projectId);
    }
}
