package io.katharsis.core.internal.dispatcher.controller.collection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.core.internal.dispatcher.controller.BaseControllerTest;
import io.katharsis.core.internal.dispatcher.controller.CollectionGet;
import io.katharsis.core.internal.dispatcher.controller.RelationshipsResourcePost;
import io.katharsis.core.internal.dispatcher.controller.ResourceGet;
import io.katharsis.core.internal.dispatcher.controller.ResourcePost;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.utils.Nullable;

public class CollectionGetTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";

	@Test
	public void onGivenRequestCollectionGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		CollectionGet sut = new CollectionGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		CollectionGet sut = new CollectionGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestCollectionGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/");
		CollectionGet sut = new CollectionGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(new QueryParams()), null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestCollectionWithIdsGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1,2");
		CollectionGet sut = new CollectionGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(new QueryParams()), null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestResourceWithIdShouldSetIt() throws Exception {
		// GIVEN
		Document Document = new Document();
		Resource data = new Resource();
		Document.setData(Nullable.of((Object) data));
		long taskId = Long.MAX_VALUE - 1L;
		data.setType("tasks");
		data.setId(Long.toString(taskId));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(new QueryParams()), null, Document);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long persistedTaskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(persistedTaskId).isEqualTo(taskId);
	}

	@Test
	public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		data = createProject();
		newProjectBody.setData(Nullable.of((Object) data));
		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(new QueryParams()), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		data = new Resource();
		newTaskToProjectBody.setData(Nullable.of((Object)Collections.singletonList(data)));
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/includedProjects");
		RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "includedProjects", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(projectId);

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]", Collections.singleton("includedProjects"));
		QueryParams queryParams1 = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(queryParams1), null, null);

		// THEN
		Assert.assertNotNull(response);
		data = response.getDocument().getSingleData().get();
		assertThat(data.getType()).isEqualTo("tasks");
		Relationship relationship = data.getRelationships().get("includedProjects");
		assertThat(relationship);
		assertThat(relationship.getCollectionData()).isNotNull();
		assertThat(relationship.getCollectionData().get().size()).isEqualTo(1);
		assertThat(relationship.getCollectionData().get().get(0).getId()).isEqualTo(projectId.toString());
	}

	@Test
	public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		data = createProject();
		newProjectBody.setData(Nullable.of((Object) data));

		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(new QueryParams()), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		data = new Resource();
		newTaskToProjectBody.setData(Nullable.of((Object) Collections.singletonList(data)));
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/projects");
		RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "projects", REQUEST_PARAMS);
		assertThat(project.getId()).isNotNull();

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]", Collections.singleton("[\"projects\"]"));
		QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(requestParams), null, null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");

		// eager loading but no inclusion
		assertThat(response.getDocument().getSingleData().get().getRelationships().get("projects").getData().isPresent()).isTrue();
	}
}
