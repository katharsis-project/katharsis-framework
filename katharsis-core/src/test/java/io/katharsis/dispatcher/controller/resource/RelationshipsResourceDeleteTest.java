package io.katharsis.dispatcher.controller.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Response;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.mock.repository.UserToProjectRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.HttpStatus;

public class RelationshipsResourceDeleteTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = HttpMethod.DELETE.name();

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final QueryParams REQUEST_PARAMS = new QueryParams();

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.buildPath("tasks/1/relationships/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

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
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onExistingToOneRelationshipShouldRemoveIt() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(data);
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.buildPath("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().getId());
		assertThat(taskId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		data = createProject();
		newProjectBody.setData(data);

		JsonPath projectPath = pathBuilder.buildPath("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(new QueryParams()), null,
				newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		data = new Resource();
		newTaskToProjectBody.setData(data);
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + taskId + "/relationships/project");
		RelationshipsResourcePost relationshipsResourcePost = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = relationshipsResourcePost.handle(savedTaskPath,
				new QueryParamsAdapter(new QueryParams()), null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(projectId);

		/* ------- */

		// GIVEN
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

		// WHEN -- removing a relation between task and project
		Response result = sut.handle(savedTaskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskToProjectBody);
		assertThat(result).isNotNull();
		taskToProjectRepository.removeRelations("project");

		// THEN
		assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
		Project nullProject = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(nullProject).isNull();
	}

	@Test
	public void onExistingToManyRelationshipShouldRemoveIt() throws Exception {
		// GIVEN
		Document newUserDocument = new Document();
		newUserDocument.setData(createUser());

		JsonPath taskPath = pathBuilder.buildPath("/users");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, OBJECT_MAPPER);

		// WHEN -- adding a user
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(new QueryParams()), null, newUserDocument);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData()).isEqualTo("users");
		Long userId = Long.parseLong(taskResponse.getDocument().getSingleData().getId());
		assertThat(userId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument = new Document();
		newProjectDocument.setData(createProject());

		JsonPath projectPath = pathBuilder.buildPath("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(new QueryParams()), null,
				newProjectDocument);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument2 = new Document();
		newProjectDocument2.setData(createProject("3"));

		JsonPath savedTaskPath = pathBuilder.buildPath("/users/" + userId + "/relationships/assignedProjects");
		RelationshipsResourcePost relationshipsResourcePost = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between user and project
		Response projectRelationshipResponse = relationshipsResourcePost.handle(savedTaskPath,
				new QueryParamsAdapter(new QueryParams()), null, newProjectDocument2);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		UserToProjectRepository userToProjectRepository = new UserToProjectRepository();
		Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
		assertThat(project.getId()).isEqualTo(projectId);

		/* ------- */

		// GIVEN
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

		// WHEN -- removing a relation between task and project
		Response result = sut.handle(savedTaskPath, new QueryParamsAdapter(new QueryParams()), null, newProjectDocument2);
		assertThat(result).isNotNull();

		// THEN
		Project nullProject = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
		assertThat(nullProject).isNull();
	}
}
