package io.katharsis.queryspec.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.AbstractQuerySpecTest;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Schedule;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.ScheduleRepository;
import io.katharsis.resource.mock.repository.ScheduleRepositoryImpl;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;

public class QuerySpecRepositoryTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter<Task, Long> taskAdapter;

	@SuppressWarnings("rawtypes")
	private RelationshipRepositoryAdapter projectRelAdapter;

	private ResourceRepositoryAdapter<Project, Serializable> projectAdapter;

	private RelationshipRepositoryAdapter<Project, Long, Task, Long> tasksRelAdapter;

	private ResourceRepositoryAdapter<Schedule, Serializable> scheduleAdapter;

	@Before
	public void setup() {
		TaskQuerySpecRepository.clear();
		ProjectQuerySpecRepository.clear();
		ScheduleRepositoryImpl.clear();

		super.setup();
		RegistryEntry<Task> taskEntry = resourceRegistry.getEntry(Task.class);
		RegistryEntry<Project> projectEntry = resourceRegistry.getEntry(Project.class);
		RegistryEntry<Schedule> scheduleEntry = resourceRegistry.getEntry(Schedule.class);
		TaskQuerySpecRepository repo = (TaskQuerySpecRepository) taskEntry.getResourceRepository(null)
				.getResourceRepository();

		repo = Mockito.spy(repo);

		
		scheduleAdapter = scheduleEntry.getResourceRepository(null);
		projectAdapter = projectEntry.getResourceRepository(null);
		taskAdapter = taskEntry.getResourceRepository(null);
		projectRelAdapter = taskEntry.getRelationshipRepositoryForClass(Project.class, null);
		tasksRelAdapter = projectEntry.getRelationshipRepositoryForClass(Task.class, null);
	}

	@Test
	public void testCrudWithQueryParamsInput() {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "sort[tasks][name]", "asc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QueryParamsAdapter queryAdapter = new QueryParamsAdapter(queryParams);
		checkCrud(queryAdapter);
	}
	
	@Test
	public void findAllWithResourceListResult() {
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		QueryAdapter adapter = new QuerySpecAdapter(querySpec, resourceRegistry);
		JsonApiResponse response = scheduleAdapter.findAll(adapter);
		Assert.assertTrue(response.getEntity() instanceof ScheduleRepository.ScheduleList);
		Assert.assertTrue(response.getLinksInformation() instanceof ScheduleRepository.ScheduleListLinks);
		Assert.assertTrue(response.getMetaInformation() instanceof ScheduleRepository.ScheduleListMeta);
	}

	@Test
	public void testCrudWithQuerySpec() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		QueryAdapter adapter = new QuerySpecAdapter(querySpec, resourceRegistry);
		checkCrud(adapter);
	}

	@SuppressWarnings({ "unchecked" })
	private void checkCrud(QueryAdapter queryAdapter) {
		// setup data
		Project project = new Project();
		project.setId(3L);
		project.setName("myProject");
		projectAdapter.create(project, queryAdapter);

		Task task = new Task();
		task.setId(2L);
		task.setName("myTask");
		task.setProject(project);
		task.setProjects(Arrays.asList(project));
		taskAdapter.create(task, queryAdapter);

		// adapter
		List<Task> tasks = (List<Task>) taskAdapter.findAll(queryAdapter).getEntity();
		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals(task, taskAdapter.findOne(2L, queryAdapter).getEntity());
		tasks = (List<Task>) taskAdapter.findAll(Arrays.asList(2L), queryAdapter).getEntity();
		Assert.assertEquals(1, tasks.size());

		// relation adapter
		projectRelAdapter.setRelation(task, project.getId(), "project", queryAdapter);
		Assert.assertNotNull(task.getProject());
		Assert.assertEquals(1, project.getTasks().size());
		JsonApiResponse response = projectRelAdapter.findOneTarget(2L, "project", queryAdapter);
		Assert.assertEquals(project.getId(), ((Project) response.getEntity()).getId());

		projectRelAdapter.setRelation(task, null, "project", queryAdapter);
		response = projectRelAdapter.findOneTarget(2L, "project", queryAdapter);
		Assert.assertNull(task.getProject());
		
		// warning: bidirectionality not properly implemented here, would require changes to the model used in many other places
		task.setProject(null);
		project.getTasks().clear();

		tasksRelAdapter.addRelations(project, Arrays.asList(task.getId()), "tasks", queryAdapter);
		Assert.assertEquals(project, task.getProject());
		Assert.assertEquals(1, project.getTasks().size());
		List<Project> projects = (List<Project>) tasksRelAdapter.findManyTargets(3L, "tasks", queryAdapter).getEntity();
		Assert.assertEquals(1, projects.size());

		tasksRelAdapter.removeRelations(project, Arrays.asList(task.getId()), "tasks", queryAdapter);
		Assert.assertEquals(0, project.getTasks().size());
		task.setProject(null); // fix bidirectionality
		
		projects = (List<Project>) tasksRelAdapter.findManyTargets(3L, "tasks", queryAdapter).getEntity();
		Assert.assertEquals(0, projects.size());

		tasksRelAdapter.setRelations(project, Arrays.asList(task.getId()), "tasks", queryAdapter);
		Assert.assertEquals(project, task.getProject());
		Assert.assertEquals(1, project.getTasks().size());
		projects = (List<Project>) tasksRelAdapter.findManyTargets(3L, "tasks", queryAdapter).getEntity();
		Assert.assertEquals(1, projects.size());

		// check bulk find
		Map<?, JsonApiResponse> bulkMap = tasksRelAdapter.findBulkManyTargets(Arrays.asList(3L), "tasks", queryAdapter);
		Assert.assertEquals(1, bulkMap.size());
		Assert.assertTrue(bulkMap.containsKey(3L));
		projects = (List<Project>) bulkMap.get(3L).getEntity();
		Assert.assertEquals(1, projects.size());

		bulkMap = projectRelAdapter.findBulkOneTargets(Arrays.asList(2L), "project", queryAdapter);
		Assert.assertEquals(1, bulkMap.size());
		Assert.assertTrue(bulkMap.containsKey(2L));
		Assert.assertNotNull(bulkMap.get(2L));

		// deletion
		taskAdapter.delete(task.getId(), queryAdapter);
		tasks = (List<Task>) taskAdapter.findAll(queryAdapter).getEntity();
		Assert.assertEquals(0, tasks.size());
		tasks = (List<Task>) taskAdapter.findAll(Arrays.asList(2L), queryAdapter).getEntity();
		Assert.assertEquals(0, tasks.size());

	}

}
