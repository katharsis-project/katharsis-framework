package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.mock.repository.UserToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RelationshipsResourcePostTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.POST.name();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final QueryParams REQUEST_PARAMS = new QueryParams();

    private UserToProjectRepository localUserToProjectRepository;

    @Before
    public void beforeTest() throws Exception {
        localUserToProjectRepository = new UserToProjectRepository();
        localUserToProjectRepository.removeRelations("project");
        localUserToProjectRepository.removeRelations("assignedProjects");
    }

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1/relationships/project");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonPath, REQUEST_TYPE, null, parameterProvider);

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onExistingResourcesShouldAddToOneRelationship() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

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

        jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        request = new Request(jsonPath, REQUEST_TYPE, serialize(newProjectBody), parameterProvider);


        // WHEN -- adding a project
        BaseResponseContext projectResponse = resourcePost.handle(request);

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

        JsonApiPath savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/relationships/project");
        request = new Request(savedTaskPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);

        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN -- adding a relation between task and project
        BaseResponseContext projectRelationshipResponse = sut.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
        Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);
    }

    @Test
    public void onExistingResourcesShouldAddToManyRelationship() throws Exception {
        // GIVEN
        RequestBody newUserBody = new RequestBody();
        DataBody data = new DataBody();
        newUserBody.setData(data);
        data.setType("users");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample user"));
        data.setRelationships(new ResourceRelationships());

        JsonApiPath savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/users");
        Request request = new Request(savedTaskPath, REQUEST_TYPE, serialize(newUserBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN -- adding a user
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(User.class);
        Long userId = ((User) (taskResponse.getResponse().getEntity())).getId();
        assertThat(userId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));


        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        request = new Request(projectPath, REQUEST_TYPE, serialize(newProjectBody), parameterProvider);


        // WHEN -- adding a project
        BaseResponseContext projectResponse = resourcePost.handle(request);

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

        savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/users/" + userId + "/relationships/assignedProjects");
        request = new Request(savedTaskPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);

        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN -- adding a relation between user and project
        BaseResponseContext projectRelationshipResponse = sut.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        UserToProjectRepository userToProjectRepository = new UserToProjectRepository();
        Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", REQUEST_PARAMS);
        assertThat(project.getId()).isEqualTo(projectId);
    }

    @Test
    public void onDeletingToOneRelationshipShouldSetTheValue() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonApiPath taskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(taskPath, REQUEST_TYPE, serialize(newTaskBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN -- adding a task
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody();
        newTaskToProjectBody.setData(null);

        JsonApiPath savedTaskPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId + "/relationships/project");
        request = new Request(savedTaskPath, REQUEST_TYPE, serialize(newTaskToProjectBody), parameterProvider);

        RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN -- adding a relation between user and project
        BaseResponseContext projectRelationshipResponse = sut.handle(request);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        assertThat(projectRelationshipResponse.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        Project project = localUserToProjectRepository.findOneTarget(1L, "project", REQUEST_PARAMS);
        assertThat(project).isNull();
    }

}
