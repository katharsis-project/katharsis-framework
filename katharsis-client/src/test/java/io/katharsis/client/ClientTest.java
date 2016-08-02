package io.katharsis.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.mock.models.Project;
import io.katharsis.client.mock.models.Task;
import io.katharsis.client.mock.repository.TaskRepository;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;

public class ClientTest extends JerseyTest {

	private KatharsisClient client;
	private ResourceRepositoryStub<Task, Long> taskRepo;
	private ResourceRepositoryStub<Project, Long> projectRepo;
	private RelationshipRepositoryStub<Task, Long, Project, Long> relRepo;

	private QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString(), "io.katharsis.client.mock");
		taskRepo = client.getRepository(Task.class);
		projectRepo = client.getRepository(Project.class);
		relRepo = client.getRepository(Task.class, Project.class);
		TaskRepository.map.clear();

		client.getHttpClient().setReadTimeout(1000000, TimeUnit.MILLISECONDS);
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private static class TestApplication extends ResourceConfig {
		public TestApplication() {
			property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.client.mock");
			property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");
			register(new KatharsisFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()),
					new SampleJsonServiceLocator()));

		}
	}

	@Test
	public void testFindEmpty() {
		List<Task> tasks = taskRepo.findAll(new QueryParams());
		Assert.assertTrue(tasks.isEmpty());
	}

	@Test
	public void testFindNull() {
		try {
			taskRepo.findOne(1L, new QueryParams());
			Assert.fail();
		} catch (ResourceException e) {
			Assert.assertEquals("Not Found", e.getMessage());
		}
	}

	@Test
	public void testSaveAndFind() {
		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.save(task);

		// check retrievable with findAll
		List<Task> tasks = taskRepo.findAll(new QueryParams());
		Assert.assertEquals(1, tasks.size());
		Task savedTask = tasks.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());

		// check retrievable with findAll(ids)
		tasks = taskRepo.findAll(Arrays.asList(1L), new QueryParams());
		Assert.assertEquals(1, tasks.size());
		savedTask = tasks.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());

		// check retrievable with findOne
		savedTask = taskRepo.findOne(1L, new QueryParams());
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());
	}

	@Test
	public void testDelete() {
		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.save(task);

		taskRepo.delete(1L);

		List<Task> tasks = taskRepo.findAll(new QueryParams());
		Assert.assertEquals(0, tasks.size());
	}

	@Test
	public void testSaveOneRelation() {
		Task task = saveTaskProject();

		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "project");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		Task savedTask = taskRepo.findOne(2L, queryParams);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());
		Assert.assertNotNull(savedTask.getProject());
		Assert.assertEquals(1L, savedTask.getProject().getId().longValue());
	}

	private Task saveTaskProject() {
		Project project = new Project();
		project.setId(1L);
		project.setName("project");
		projectRepo.save(project);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		task.setProject(project);
		taskRepo.save(task);
		return task;
	}

	@Test
	public void testSetRelation() {
		Project project = new Project();
		project.setId(1L);
		project.setName("project");
		projectRepo.save(project);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.save(task);

		relRepo.setRelation(task, project.getId(), "project");

		Project relProject = relRepo.findOneTarget(task.getId(), "project", new QueryParams());
		Assert.assertNotNull(relProject);
		Assert.assertEquals(project.getId(), relProject.getId());
	}

	@Test
	public void testAddSetRemoveRelations() {
		Project project0 = new Project();
		project0.setId(1L);
		project0.setName("project0");
		projectRepo.save(project0);

		Project project1 = new Project();
		project1.setId(2L);
		project1.setName("project1");
		projectRepo.save(project1);

		Task task = new Task();
		task.setId(3L);
		task.setName("test");
		taskRepo.save(task);

		relRepo.addRelations(task, Arrays.asList(project0.getId(), project1.getId()), "projects");
		List<Project> relProjects = relRepo.findManyTargets(task.getId(), "projects", new QueryParams());
		Assert.assertEquals(2, relProjects.size());

		relRepo.setRelations(task, Arrays.asList(project1.getId()), "projects");
		relProjects = relRepo.findManyTargets(task.getId(), "projects", new QueryParams());
		Assert.assertEquals(1, relProjects.size());
		Assert.assertEquals(project1.getId(), relProjects.get(0).getId());

		// FIXME HTTP DELETE method with payload not supported? at least in
		// Jersey
		// relRepo.removeRelations(task, Arrays.asList(project1.getId()),
		// "projects");
		// relProjects = relRepo.findManyTargets(task.getId(), "projects", new
		// QueryParams());
		// Assert.assertEquals(0, relProjects.size());
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}
}