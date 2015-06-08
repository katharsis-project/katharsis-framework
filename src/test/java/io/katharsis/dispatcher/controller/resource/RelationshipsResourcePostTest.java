package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.Attributes;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RelationshipsResourcePostTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.POST.name();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/relationships/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onExistingParentResourceShouldSaveIt() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        newTaskBody.setData(new DataBody());
        newTaskBody.getData().setType("tasks");
        newTaskBody.getData().setAttributes(new Attributes());
        newTaskBody.getData().getAttributes().addAttribute("name", "sample task");
        newTaskBody.getData().setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser);

        // WHEN -- adding a task
        BaseResponse taskResponse = resourcePost.handle(taskPath, new RequestParams(new ObjectMapper()), newTaskBody);

        // THEN
        assertThat(taskResponse.getData()).isExactlyInstanceOf(Container.class);
        assertThat(((Container) taskResponse.getData()).getData()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (((Container) taskResponse.getData()).getData())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        newProjectBody.setData(new DataBody());
        newProjectBody.getData().setType("projects");
        newProjectBody.getData().setAttributes(new Attributes());
        newProjectBody.getData().getAttributes().addAttribute("name", "sample project");

        JsonPath projectPath = pathBuilder.buildPath("/projects");

        // WHEN -- adding a project
        ResourceResponse projectResponse = resourcePost.handle(projectPath, new RequestParams(OBJECT_MAPPER), newProjectBody);

        // THEN
        assertThat(projectResponse.getData()).isExactlyInstanceOf(Container.class);
        assertThat(((Container) projectResponse.getData()).getData()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (((Container) projectResponse.getData()).getData())).getId()).isNotNull();
        assertThat(((Project) (((Container) projectResponse.getData()).getData())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (((Container) projectResponse.getData()).getData())).getId();
        assertThat(projectId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody();
        newTaskToProjectBody.setData(new DataBody());
        newTaskToProjectBody.getData().setType("projects");
        newTaskToProjectBody.getData().setId(projectId.toString());

        JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId);
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

        // WHEN -- adding a relation between task and project
        BaseResponse projectRelationshipResponse = sut.handle(savedTaskPath, new RequestParams(OBJECT_MAPPER), newProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "project");
        assertThat(project.getId()).isEqualTo(projectId);
    }
}
