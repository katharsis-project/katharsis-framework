package io.katharsis.dispatcher.controller.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.dispatcher.controller.Response;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;

public class ResourceGetTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";

	@Before
	public void before() throws JsonProcessingException, IOException {
		this.prepare();
	}

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		ResourceGet sut = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		ResourceGet sut = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(data);

		JsonPath taskPath = pathBuilder.buildPath("/tasks");

		// WHEN
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);
		assertThat(taskResponse.getDocument().getData()).isExactlyInstanceOf(Resource.class);
		String taskId = ((Resource) taskResponse.getDocument().getData()).getId();
		assertThat(taskId).isNotNull();

		// GIVEN
		JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
		ResourceGet sut = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
		// GIVEN
		JsonPath jsonPath = pathBuilder.buildPath("/task-with-lookup/1");
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[task-with-lookup]", new HashSet<>(Arrays.asList("project", "projectNull", "projectOverridden", "projectOverriddenNull")));
		QueryParams queryParamsObject = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(queryParamsObject), null, null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getData()).isExactlyInstanceOf(Resource.class);
		assertThat(response.getDocument().getSingleData().getType()).isEqualTo("task-with-lookup");
		Resource responseData = response.getDocument().getSingleData();
		assertThat(responseData.getRelationships().get("project").getSingleData().get().getId()).isEqualTo("42");
		assertThat(responseData.getRelationships().get("projectNull").getSingleData().get().getId()).isEqualTo("1");
		assertThat(responseData.getRelationships().get("projectOverridden").getSingleData().get().getId()).isEqualTo("1");
		assertThat(responseData.getRelationships().get("projectOverriddenNull").getSingleData().get().getId()).isEqualTo("1");
	}

	@Test
	public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(data);

		JsonPath taskPath = pathBuilder.buildPath("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData()).isExactlyInstanceOf(Resource.class);
		assertThat(taskResponse.getDocument().getSingleData().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().getId()).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		newProjectBody.setData(createProject());

		JsonPath projectPath = pathBuilder.buildPath("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData()).isExactlyInstanceOf(Resource.class);
		assertThat(projectResponse.getDocument().getSingleData().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().getAttributes().get("name").asText()).isEqualTo("sample project");

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		ResourceId reldata = new ResourceId();
		newTaskToProjectBody.setData(data);
		data.setType("projects");
		data.setId("2");

		JsonPath savedTaskPath = pathBuilder.buildPath("/tasks/" + TASK_ID + "/relationships/project");
		RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(TASK_ID, "project", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(PROJECT_ID);

		// Given
		JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + TASK_ID);
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]", Collections.singleton("[\"project\"]"));
		QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(requestParams), null, null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().getRelationships().get("project").getData().get()).isNull();
	}

}
