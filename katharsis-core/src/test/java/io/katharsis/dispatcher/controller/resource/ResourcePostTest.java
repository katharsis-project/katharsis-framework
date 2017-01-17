package io.katharsis.dispatcher.controller.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.Response;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Pojo;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.PojoRepository;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.response.HttpStatus;
import io.katharsis.utils.java.Nullable;

public class ResourcePostTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "POST";

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/1");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onInconsistentResourceTypesShouldThrowException() throws Exception {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of((Object)data));

		JsonPath projectPath = pathBuilder.build("/tasks");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// THEN
		expectedException.expect(RuntimeException.class);

		// WHEN
		sut.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);
	}

	@Test
	public void onNonExistentResourceShouldThrowException() throws Exception {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		data.setType("fridges");
		newProjectBody.setData(Nullable.of((Object)data));

		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// THEN
		expectedException.expect(ResourceNotFoundException.class);

		// WHEN
		sut.handle(new ResourcePath("fridges"), new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);
	}

	@Test
	public void onNoBodyResourceShouldThrowException() throws Exception {
		// GIVEN
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// THEN
		expectedException.expect(RuntimeException.class);

		// WHEN
		sut.handle(new ResourcePath("fridges"), new QueryParamsAdapter(REQUEST_PARAMS), null, null);
	}

	@Test
	public void onNewResourcesAndRelationshipShouldPersistThoseData() throws Exception {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of((Object)data));

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN
		Response projectResponse = sut.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(projectResponse.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		Resource persistedProject = projectResponse.getDocument().getSingleData().get();
		assertThat(persistedProject.getId()).isNotNull();
		assertThat(persistedProject.getAttributes().get("name").asText()).isEqualTo("sample project");
		assertThat(persistedProject.getAttributes().get("data").get("data").asText()).isEqualTo("asd");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());

		/* ------- */

		// GIVEN
		Document newTasksBody = new Document();
		newTasksBody.setData(Nullable.of((Object)createTask()));
		newTasksBody.getSingleData().get().getRelationships().put("project",
				new Relationship(new ResourceIdentifier(projectId.toString(), "projects")));

		JsonPath taskPath = pathBuilder.build("/tasks");

		// WHEN
		Response taskResponse = sut.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTasksBody);

		// THEN
		assertThat(taskResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		String taskId = taskResponse.getDocument().getSingleData().get().getId();
		assertThat(taskId).isNotNull();
		assertThat(taskResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample task");

		TaskRepository taskRepository = new TaskRepository();
		Task persistedTask = taskRepository.findOne(Long.parseLong(taskId), null);
		assertThat(persistedTask.getProject().getId()).isEqualTo(projectId);
	}

	@Test
	public void onNewResourcesAndRelationshipsShouldPersistThoseData() throws Exception {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of((Object)data));
		data.setType("projects");

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN
		Response projectResponse = sut.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());

		/* ------- */

		// GIVEN
		Document newUserBody = new Document();
		data = new Resource();
		newUserBody.setData(Nullable.of((Object)data));
		data.setType("users");
		data.setAttribute("name", objectMapper.readTree("\"some user\""));
		data.getRelationships().put("assignedProjects",
				new Relationship(Collections.singletonList(new ResourceIdentifier(projectId.toString(), "projects"))));

		JsonPath taskPath = pathBuilder.build("/users");

		// WHEN
		Response taskResponse = sut.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newUserBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("users");
		Long userId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(userId).isNotNull();
		assertThat(taskResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("some user");

		assertThat(taskResponse.getDocument().getSingleData().get().getRelationships().get("assignedProjects").getCollectionData().get())
				.hasSize(1);
		assertThat(taskResponse.getDocument().getSingleData().get().getRelationships().get("assignedProjects").getCollectionData().get()
				.get(0).getId()).isEqualTo(projectId.toString());
	}

	@Test
	public void onNewInheritedResourceShouldPersistThisResource() throws Exception {
		// GIVEN
		Document newMemorandumBody = new Document();
		Resource data = new Resource();
		newMemorandumBody.setData(Nullable.of((Object)data));
		data.setType("memoranda");
		data.setAttribute("title", objectMapper.readTree("\"sample title\""));
		data.setAttribute("body", objectMapper.readTree("\"sample body\""));

		JsonPath projectPath = pathBuilder.build("/documents");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN
		Response memorandumResponse = sut.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newMemorandumBody);

		// THEN
		assertThat(memorandumResponse.getDocument().getSingleData().get().getType()).isEqualTo("memoranda");
		Resource persistedMemorandum = memorandumResponse.getDocument().getSingleData().get();
		assertThat(persistedMemorandum.getId()).isNotNull();
		assertThat(persistedMemorandum.getAttributes().get("title").asText()).isEqualTo("sample title");
		assertThat(persistedMemorandum.getAttributes().get("body").asText()).isEqualTo("sample body");
	}

	@Test
	public void onResourceWithCustomNamesShouldSaveParametersCorrectly() throws Exception {
		// GIVEN - creating sample project id
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of((Object)data));

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN
		Response projectResponse = sut.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());

		/* ------- */

		// GIVEN
		Document pojoBody = new Document();
		Resource pojoData = new Resource();
		pojoBody.setData(Nullable.of((Object)pojoData));
		pojoData.setType("pojo");
		JsonNode put = objectMapper.createObjectNode().put("value", "hello");
		pojoData.setAttribute("other-pojo", put);
		pojoData.getRelationships().put("some-project", new Relationship(new ResourceIdentifier(Long.toString(projectId), "projects")));
		pojoData.getRelationships().put("some-projects",
				new Relationship(Arrays.asList(new ResourceIdentifier(Long.toString(projectId), "projects"))));

		JsonPath pojoPath = pathBuilder.build("/pojo");

		// WHEN
		QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(Collections.singletonMap("include[pojo]", Collections.singleton("projects")));
		Response pojoResponse = sut.handle(pojoPath, new QueryParamsAdapter(queryParams), null, pojoBody);

		// THEN
		assertThat(pojoResponse.getDocument().getSingleData().get().getType()).isEqualTo("pojo");
		Resource persistedPojo = pojoResponse.getDocument().getSingleData().get();
		assertThat(persistedPojo.getId()).isNotNull();
		assertThat(persistedPojo.getAttributes().get("other-pojo").get("value").asText()).isEqualTo("hello");
		assertThat(persistedPojo.getRelationships().get("some-project").getSingleData().get()).isNotNull();
		assertThat(persistedPojo.getRelationships().get("some-project").getSingleData().get().getId()).isEqualTo(projectId.toString());
		Relationship persistedProjectsRelationship = persistedPojo.getRelationships().get("some-projects");
		assertThat(persistedProjectsRelationship).isNotNull();
		
		// check lazy loaded relation
		PojoRepository repo = (PojoRepository) resourceRegistry.getEntry(Pojo.class).getResourceRepository(null).getResourceRepository();
		Pojo pojo = repo.findOne(null, null);
		assertThat(pojo.getProjects()).hasSize(1);
		assertThat(pojo.getProjects().get(0).getId()).isEqualTo(projectId);
	}

	@Test
	public void onResourceWithInvalidRelationshipNameShouldThrowException() throws Exception {
		// GIVEN - creating sample project id
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of((Object)data));

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN
		Response projectResponse = sut.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());

		/* ------- */

		// GIVEN
		Document pojoBody = new Document();
		Resource pojoData = new Resource();
		pojoBody.setData(Nullable.of((Object)pojoData));
		pojoData.setType("pojo");
		JsonNode put = objectMapper.createObjectNode().put("value", "hello");
		pojoData.setAttribute("other-pojo", objectMapper.readTree("null"));
		String invalidRelationshipName = "invalid-relationship";
		pojoData.getRelationships().put(invalidRelationshipName, new Relationship(new ResourceIdentifier(Long.toString(projectId), "projects")));

		JsonPath pojoPath = pathBuilder.build("/pojo");

		// THEN
		expectedException.expect(ResourceException.class);
		expectedException.expectMessage(String.format("Invalid relationship name: %s", invalidRelationshipName));

		// WHEN
		sut.handle(pojoPath, new QueryParamsAdapter(REQUEST_PARAMS), null, pojoBody);
	}
}
