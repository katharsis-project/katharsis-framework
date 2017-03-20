package io.katharsis.core.internal.dispatcher.controller.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.core.internal.dispatcher.controller.BaseControllerTest;
import io.katharsis.core.internal.dispatcher.controller.RelationshipsResourcePost;
import io.katharsis.core.internal.dispatcher.controller.ResourceGet;
import io.katharsis.core.internal.dispatcher.controller.ResourcePost;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Group;
import io.katharsis.resource.mock.models.Memorandum;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.utils.Nullable;

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
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.buildPath("/tasks");

		// WHEN
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);
		assertThat(taskResponse.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		String taskId = ((Resource) taskResponse.getDocument().getData().get()).getId();
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
		assertThat(response.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("task-with-lookup");
		Resource responseData = response.getDocument().getSingleData().get();
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
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.buildPath("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().get().getId()).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		newProjectBody.setData(Nullable.of((Object) createProject()));

		JsonPath projectPath = pathBuilder.buildPath("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		ResourceIdentifier reldata = new ResourceIdentifier();
		newTaskToProjectBody.setData(Nullable.of((Object) data));
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
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().get().getRelationships().get("project").getData().get()).isNull();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testIncludeMultipleRelations() {
		// get repositories
		ResourceRepositoryAdapter userRepo = resourceRegistry.findEntry(User.class).getResourceRepository(null);
		ResourceRepositoryAdapter projectRepo = resourceRegistry.findEntry(Project.class).getResourceRepository(null);
		ResourceRepositoryAdapter memoRepo = resourceRegistry.findEntry(Memorandum.class).getResourceRepository(null);
		ResourceRepositoryAdapter groupRepo = resourceRegistry.findEntry(Group.class).getResourceRepository(null);

		RelationshipRepositoryAdapter relRepositoryUserToGroup = resourceRegistry.findEntry(User.class).getRelationshipRepositoryForClass(Group.class, null);
		RelationshipRepositoryAdapter relRepositoryGroupToProject = resourceRegistry.findEntry(Group.class)
				.getRelationshipRepositoryForClass(Project.class, null);

		RelationshipRepositoryAdapter relRepositoryGroupToMemo = resourceRegistry.findEntry(Group.class)
				.getRelationshipRepositoryForClass(Memorandum.class, null);

		ResourceInformation groupInfo = resourceRegistry.findEntry(Group.class).getResourceInformation();
		ResourceField groupProjectsField = groupInfo.findRelationshipFieldByName("projects");
		ResourceField groupMemosField = groupInfo.findRelationshipFieldByName("memoranda");

		ResourceInformation userInfo = resourceRegistry.findEntry(User.class).getResourceInformation();
		ResourceField userGroupField = userInfo.findRelationshipFieldByName("group");

		// setup test data
		// a User with a group, and a group that has one memo and one document

		// - a user
		User user = new User();
		user.setId(1L);
		userRepo.create(user, null);

		// - a group
		Group group = new Group();
		group.setId(1L);
		groupRepo.create(group, null);

		// - a project
		Project project = new Project();
		project.setId(1L);
		project.setName("a project");
		projectRepo.create(project, null);

		// - a memo
		Memorandum memo = new Memorandum();
		memo.setId(1L);
		memo.setTitle("TPS Report reminder");
		memo.setBody("don't forget those tps reports");
		memoRepo.create(memo, null);

		// - add the user to the group
		relRepositoryUserToGroup.setRelation(user, group.getId(), userGroupField, null);
		// - add the project to the group
		relRepositoryGroupToProject.setRelations(group, Collections.singleton(project.getId()), groupProjectsField, null);
		// - add the memo to the group
		relRepositoryGroupToMemo.setRelations(group, Collections.singleton(memo.getId()), groupMemosField, null);

		Map<String, Set<String>> params = new HashMap<>();
		addParams(params, "include[users]", "group");
		addParams(params, "include[users]", "group.projects");
		addParams(params, "include[users]", "group.memoranda");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(userInfo, queryParams, resourceRegistry);
		JsonPath jsonPath = pathBuilder.buildPath("/users/1");
		ResourceGet sut = new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);
		Response response = sut.handle(jsonPath, queryAdapter, null, null);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getDocument().getData());
		List<Resource> included = response.getDocument().getIncluded();
		List<String> expectedIncludeTypes = Arrays.asList("groups", "projects", "memoranda");

		List<String> includedTypes = new ArrayList<>();
		for (Resource resource : included) {
			includedTypes.add(resource.toIdentifier().getType());
		}

		for (String expectedIncludeType : expectedIncludeTypes) {
			Assert.assertTrue("included resources must contain type: " + expectedIncludeType +
							" but contains only: " + includedTypes.toString(),
					includedTypes.contains(expectedIncludeType));
		}
		Assert.assertEquals(3, included.size());
	}
}
