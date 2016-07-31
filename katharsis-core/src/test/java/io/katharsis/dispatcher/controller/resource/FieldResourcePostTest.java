package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourcePostTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.POST.name();

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1/project");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        FieldResourcePost sut = new FieldResourcePost(resourceRegistry, typeParser, queryParamsBuilder,objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onRelationshipRequestShouldDenyIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        FieldResourcePost sut = new FieldResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

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
        FieldResourcePost sut = new FieldResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onExistingParentResourceShouldSaveIt() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody(DataBody.builder()
                .type("tasks")
                .attributes(objectMapper.createObjectNode().put("name", "sample task"))
                .relationships(new ResourceRelationships())
                .build());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(taskPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody(DataBody.builder()
                .type("projects")
                .attributes(objectMapper.createObjectNode().put("name", "sample project"))
                .build());

        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/project");
        request = new Request(projectPath, REQUEST_TYPE, serialize(newProjectBody), parameterProvider);
        FieldResourcePost sut = new FieldResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext projectResponse = sut.handle(request);

        // THEN
        assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();

        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);
    }

    @Test
    public void onExistingParentResourceShouldSaveToToMany() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody(DataBody.builder()
                .type("tasks")
                .attributes(objectMapper.createObjectNode().put("name", "sample task"))
                .relationships(new ResourceRelationships())
                .build());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(taskPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody(DataBody.builder()
                .type("projects")
                .attributes(objectMapper.createObjectNode().put("name", "sample project"))
                .build());
        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/projects");
        request = new Request(projectPath, REQUEST_TYPE, serialize(newProjectBody), parameterProvider);
        FieldResourcePost sut = new FieldResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext projectResponse = sut.handle(request);

        // THEN
        assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();

        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "projects", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);
    }
}
