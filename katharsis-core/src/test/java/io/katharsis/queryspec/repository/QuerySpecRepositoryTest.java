package io.katharsis.queryspec.repository;

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
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;

public class QuerySpecRepositoryTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter<Task, Long> adapter;

	@SuppressWarnings("rawtypes")
	private RelationshipRepositoryAdapter relAdapter;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		TestQuerySpecResourceRepository.clear();
		TestQuerySpecRelationshipRepository.clear();

		super.setup();
		RegistryEntry<Task> registryEntry = resourceRegistry.getEntry(Task.class);
		TestQuerySpecResourceRepository repo = (TestQuerySpecResourceRepository) registryEntry.getResourceRepository(null)
				.getResourceRepository();

		repo = Mockito.spy(repo);

		adapter = registryEntry.getResourceRepository(null);
		relAdapter = registryEntry.getRelationshipRepositoryForClass(Project.class, null);
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
	public void testCrudWithNullInput() {
		checkCrud(null);
	}

	@SuppressWarnings({ "unchecked" })
	private void checkCrud(QueryAdapter queryAdapter) {
		// setup data
		Project project = new Project();
		project.setId(3L);
		project.setName("myProject");

		Task task = new Task();
		task.setId(2L);
		task.setName("myTask");
		task.setProject(project);
		task.setProjects(Arrays.asList(project));
		adapter.create(task, queryAdapter);

		// adapter
		List<Task> tasks = (List<Task>) adapter.findAll(queryAdapter).getEntity();
		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals(task, adapter.findOne(2L, queryAdapter).getEntity());
		tasks = (List<Task>) adapter.findAll(Arrays.asList(2L), queryAdapter).getEntity();
		Assert.assertEquals(1, tasks.size());

		// relation adapter
		relAdapter.setRelation(task, project.getId(), "project", queryAdapter);
		JsonApiResponse response = relAdapter.findOneTarget(2L, "project", queryAdapter);
		Assert.assertEquals(project.getId(), ((Project) response.getEntity()).getId());

		relAdapter.setRelation(task, null, "project", queryAdapter);
		response = relAdapter.findOneTarget(2L, "project", queryAdapter);
		Assert.assertNull(response.getEntity());

		relAdapter.addRelations(task, Arrays.asList(project.getId()), "projects", queryAdapter);
		List<Project> projects = (List<Project>) relAdapter.findManyTargets(2L, "projects", queryAdapter).getEntity();
		Assert.assertEquals(1, projects.size());

		relAdapter.removeRelations(task, Arrays.asList(project.getId()), "projects", queryAdapter);
		projects = (List<Project>) relAdapter.findManyTargets(2L, "projects", queryAdapter).getEntity();
		Assert.assertEquals(0, projects.size());

		relAdapter.setRelations(task, Arrays.asList(project.getId()), "projects", queryAdapter);
		projects = (List<Project>) relAdapter.findManyTargets(2L, "projects", queryAdapter).getEntity();
		Assert.assertEquals(1, projects.size());

		// check bulk find
		Map<?, JsonApiResponse>  bulkMap = relAdapter.findBulkManyTargets(Arrays.asList(2L), "projects", queryAdapter);
		Assert.assertEquals(1, bulkMap.size());
		Assert.assertTrue(bulkMap.containsKey(2L));
		projects = (List<Project>) bulkMap.get(2L).getEntity();
		Assert.assertEquals(1, projects.size());
		
		bulkMap = relAdapter.findBulkOneTargets(Arrays.asList(2L), "project", queryAdapter);
		Assert.assertEquals(1, bulkMap.size());
		Assert.assertTrue(bulkMap.containsKey(2L));
		Assert.assertNotNull(bulkMap.get(2L));
		
		
		// deletion
		adapter.delete(task.getId(), queryAdapter);
		tasks = (List<Task>) adapter.findAll(queryAdapter).getEntity();
		Assert.assertEquals(0, tasks.size());
		Assert.assertNull(adapter.findOne(2L, queryAdapter).getEntity());
		tasks = (List<Task>) adapter.findAll(Arrays.asList(2L), queryAdapter).getEntity();
		Assert.assertEquals(0, tasks.size());
		
	}

}
