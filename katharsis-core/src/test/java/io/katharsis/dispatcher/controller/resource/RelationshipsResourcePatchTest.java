package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.internal.core.BaseResponseContext;
import io.katharsis.client.internal.core.DataBody;
import io.katharsis.client.internal.core.LinkageData;
import io.katharsis.client.internal.core.RequestBody;
import io.katharsis.client.internal.core.ResourceRelationships;
import io.katharsis.client.internal.core.ResourceResponseContext;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.ProjectPolymorphic;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.mock.repository.UserToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.HttpStatus;
import io.katharsis.utils.ClassUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class RelationshipsResourcePatchTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = HttpMethod.PATCH.name();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/relationships/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        RelationshipsResourcePatch sut = new RelationshipsResourcePatch(resourceRegistry, typeParser);

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
        RelationshipsResourcePatch sut = new RelationshipsResourcePatch(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

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
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/tasks");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER);

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
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample project"));

        JsonPath projectPath = pathBuilder.buildPath("/projects");

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

        JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId + "/relationships/project");
        RelationshipsResourcePatch sut = new RelationshipsResourcePatch(resourceRegistry, typeParser);

        // WHEN -- adding a relation between task and project
        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskToProjectBody);
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
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample user"));
        data.setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/users");
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER);

        // WHEN -- adding a user
        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newUserBody);

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
        data.setAttributes(OBJECT_MAPPER.createObjectNode().put("name", "sample project"));

        JsonPath projectPath = pathBuilder.buildPath("/projects");

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
        newTaskToProjectBody.setData(Collections.singletonList(data));
        data.setType("projects");
        data.setId(projectId.toString());

        JsonPath savedTaskPath = pathBuilder.buildPath("/users/" + userId + "/relationships/assignedProjects");
        RelationshipsResourcePatch sut = new RelationshipsResourcePatch(resourceRegistry, typeParser);

        // WHEN -- adding a relation between user and project
        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        UserToProjectRepository userToProjectRepository = new UserToProjectRepository();
        Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
        assertThat(project.getId()).isEqualTo(projectId);
    }

    @Test
    public void onDeletingToOneRelationshipShouldSetTheValue() throws Exception {
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
        BaseResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        /* ------- */

        // GIVEN
        RequestBody newTaskToProjectBody = new RequestBody();
        newTaskToProjectBody.setData(null);

        JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId + "/relationships/project");
        RelationshipsResourcePatch sut = new RelationshipsResourcePatch(resourceRegistry, typeParser);

        // WHEN -- adding a relation between user and project
        BaseResponseContext projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(new QueryParams()), null,
                newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        assertThat(projectRelationshipResponse.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        Project project = localUserToProjectRepository.findOneTarget(1L, "project", new QuerySpec(Project.class));
        assertThat(project).isNull();
    }

    @Test
    public void supportPolymorphicRelationshipTypes() {

        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        data.setType(ClassUtils.getAnnotation(Task.class, JsonApiResource.class).get().type());
        data.setAttributes(objectMapper.createObjectNode());
        newTaskBody.setData(data);

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);
        ResourceResponseContext taskResponse = resourcePost.handle(taskPath,
                new QueryParamsAdapter(REQUEST_PARAMS),
                null,
                newTaskBody);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskIdOne = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskIdOne).isNotNull();
        taskResponse = resourcePost.handle(taskPath,
                new QueryParamsAdapter(REQUEST_PARAMS),
                null,
                newTaskBody);
        Long taskIdTwo = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskIdOne).isNotNull();
        taskResponse = resourcePost.handle(taskPath,
                new QueryParamsAdapter(REQUEST_PARAMS),
                null,
                newTaskBody);
        Long taskIdThree = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskIdOne).isNotNull();
        newTaskBody = new RequestBody();

        // Create ProjectPolymorphic object
        RequestBody newProjectBody = new RequestBody();
        data = new DataBody();
        String type = ClassUtils.getAnnotation(ProjectPolymorphic.class, JsonApiResource.class).get().type();
        data.setType(type);
        data.setAttributes(objectMapper.createObjectNode());
        ResourceRelationships resourceRelationships = new ResourceRelationships();
        resourceRelationships.setAdditionalProperty("task", new LinkageData("tasks", taskIdOne.toString()));
        List<LinkageData> linkageDataList = new ArrayList<>();
        linkageDataList.add(new LinkageData("tasks", taskIdTwo.toString()));
        linkageDataList.add(new LinkageData("tasks", taskIdThree.toString()));
        resourceRelationships.setAdditionalProperty("tasks", linkageDataList);
        data.setRelationships(resourceRelationships);
        newProjectBody.setData(data);
        JsonPath projectPolymorphicTypePath = pathBuilder.buildPath("/" + type);


        ResourceResponseContext projectResponse = resourcePost.handle(projectPolymorphicTypePath,
                new QueryParamsAdapter(REQUEST_PARAMS),
                null,
                newProjectBody);

        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(ProjectPolymorphic.class);
        Long projectId = ((ProjectPolymorphic) (projectResponse.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();
        ProjectPolymorphic projectPolymorphic = (ProjectPolymorphic) projectResponse.getResponse()
                .getEntity();
        assertNotNull(projectPolymorphic.getTask());
        assertNotNull(projectPolymorphic.getTasks());
        assertEquals(2, projectPolymorphic.getTasks().size());

        projectPolymorphicTypePath = pathBuilder.buildPath("/" + type + "/" + projectPolymorphic.getId());
        ResourcePatch resourcePatch = new ResourcePatch(resourceRegistry, typeParser, objectMapper);
        data = (DataBody) newProjectBody.getData();
        data.setId(projectId.toString());
        projectPolymorphic.setId(projectId);
        ((List) resourceRelationships.getAdditionalProperties().get("tasks")).clear();

        // WHEN
        BaseResponseContext baseResponseContext = resourcePatch.handle(projectPolymorphicTypePath,
                new QueryParamsAdapter(REQUEST_PARAMS),
                null,
                newProjectBody);
        assertThat(baseResponseContext.getResponse().getEntity()).isExactlyInstanceOf(ProjectPolymorphic.class);
        projectId = ((ProjectPolymorphic) (baseResponseContext.getResponse().getEntity())).getId();
        assertThat(projectId).isNotNull();
        projectPolymorphic = (ProjectPolymorphic) baseResponseContext.getResponse()
                .getEntity();
        assertNotNull(projectPolymorphic.getTask());
        assertNotNull(projectPolymorphic.getTasks());
        assertEquals(0, projectPolymorphic.getTasks().size());
    }
}
