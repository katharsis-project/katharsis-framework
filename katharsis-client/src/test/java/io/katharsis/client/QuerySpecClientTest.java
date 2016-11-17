package io.katharsis.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapterListener;
import io.katharsis.client.mock.models.Project;
import io.katharsis.client.mock.models.Schedule;
import io.katharsis.client.mock.models.Task;
import io.katharsis.client.mock.repository.ScheduleRepository;
import io.katharsis.client.mock.repository.ScheduleRepository.ScheduleList;
import io.katharsis.client.mock.repository.ScheduleRepository.ScheduleListLinks;
import io.katharsis.client.mock.repository.ScheduleRepository.ScheduleListMeta;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

public class QuerySpecClientTest extends AbstractClientTest {

	protected QuerySpecResourceRepositoryStub<Task, Long> taskRepo;

	protected QuerySpecResourceRepositoryStub<Project, Long> projectRepo;

	protected QuerySpecRelationshipRepositoryStub<Task, Long, Project, Long> relRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getQuerySpecRepository(Task.class);
		projectRepo = client.getQuerySpecRepository(Project.class);
		relRepo = client.getQuerySpecRepository(Task.class, Project.class);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(true);
	}

	@Test
	public void testInterfaceAccess() {
		ScheduleRepository scheduleRepository = client.getResourceRepository(ScheduleRepository.class);

		Schedule schedule = new Schedule();
		schedule.setId(13L);
		schedule.setName("mySchedule");
		scheduleRepository.save(schedule);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		ScheduleList list = scheduleRepository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		ScheduleListMeta meta = list.getMeta();
		ScheduleListLinks links = list.getLinks();
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);
	}

	@Test
	public void testSortAsc() {
		for (int i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(Long.valueOf(i));
			task.setName("task" + i);
			taskRepo.create(task);
		}
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(5, tasks.size());
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals("task" + i, tasks.get(i).getName());
		}
	}

	@Test
	public void testSortDesc() {
		for (int i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(Long.valueOf(i));
			task.setName("task" + i);
			taskRepo.create(task);
		}
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(5, tasks.size());
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals("task" + i, tasks.get(4 - i).getName());
		}
	}

	@Test
	public void testFindEmpty() {
		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertTrue(tasks.isEmpty());
	}

	@Test
	public void testFindNull() {
		try {
			taskRepo.findOne(1L, new QuerySpec(Task.class));
			Assert.fail();
		}
		catch (ClientException e) {
			Assert.assertEquals("Not Found", e.getMessage());
		}
	}

	@Test
	public void testCreateAndFind() {
		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);

		// check retrievable with findAll
		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(1, tasks.size());
		Task savedTask = tasks.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());

		// check retrievable with findAll(ids)
		tasks = taskRepo.findAll(Arrays.asList(1L), new QuerySpec(Task.class));
		Assert.assertEquals(1, tasks.size());
		savedTask = tasks.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());

		// check retrievable with findOne
		savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());
	}

	@Test
	public void testUpdatePushAlways() {
		client.setPushAlways(true);
		testUpdate(true);
	}

	@Test
	public void testUpdate() {
		client.setPushAlways(false);
		testUpdate(false);
	}

	public void testUpdate(boolean pushAlways) {
		final List<String> methods = new ArrayList<>();
		final List<String> paths = new ArrayList<>();
		final Interceptor interceptor = new Interceptor() {

			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();

				methods.add(request.method());
				paths.add(request.url().encodedPath());

				return chain.proceed(request);
			}
		};

		HttpAdapter httpAdapter = client.getHttpAdapter();
		if (httpAdapter instanceof OkHttpAdapter) {
			((OkHttpAdapter) httpAdapter).addListener(new OkHttpAdapterListener() {

				@Override
				public void onBuild(Builder builder) {
					builder.addInterceptor(interceptor);
				}
			});
		}

		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);

		Task savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
		Assert.assertNotNull(savedTask);

		// perform update
		task.setName("updatedName");
		taskRepo.save(task);

		// check updated
		savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
		Assert.assertNotNull(savedTask);
		Assert.assertEquals("updatedName", task.getName());

		if (httpAdapter instanceof OkHttpAdapter) {
			// check HTTP handling
			Assert.assertEquals(4, methods.size());
			Assert.assertEquals(4, paths.size());
			Assert.assertEquals("POST", methods.get(0));
			Assert.assertEquals("GET", methods.get(1));
			if (pushAlways) {
				Assert.assertEquals("POST", methods.get(2));
				Assert.assertEquals("/tasks/", paths.get(2));
			}
			else {
				Assert.assertEquals("PATCH", methods.get(2));
				Assert.assertEquals("/tasks/1/", paths.get(2));
			}
			Assert.assertEquals("GET", methods.get(3));

			Assert.assertEquals("/tasks/", paths.get(0));
			Assert.assertEquals("/tasks/1/", paths.get(1));
			Assert.assertEquals("/tasks/1/", paths.get(3));
		}
	}

	@Test
	public void testGeneratedId() {
		Task task = new Task();
		task.setId(null);
		task.setName("test");
		Task savedTask = taskRepo.create(task);
		Assert.assertNotNull(savedTask.getId());
	}

	@Test
	public void testDelete() {
		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);

		taskRepo.delete(1L);

		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(0, tasks.size());
	}

	@Test
	@Ignore // get rid of queryparams
	public void testSetRelation() {
		Project project = new Project();
		project.setId(1L);
		project.setName("project");
		projectRepo.create(project);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		relRepo.setRelation(task, project.getId(), "project");

		Project relProject = relRepo.findOneTarget(task.getId(), "project", new QuerySpec(Task.class));
		Assert.assertNotNull(relProject);
		Assert.assertEquals(project.getId(), relProject.getId());
	}

	@Test
	@Ignore // get rid of queryparams
	public void testAddSetRemoveRelations() {
		Project project0 = new Project();
		project0.setId(1L);
		project0.setName("project0");
		projectRepo.create(project0);

		Project project1 = new Project();
		project1.setId(2L);
		project1.setName("project1");
		projectRepo.create(project1);

		Task task = new Task();
		task.setId(3L);
		task.setName("test");
		taskRepo.create(task);

		relRepo.addRelations(task, Arrays.asList(project0.getId(), project1.getId()), "projects");
		List<Project> relProjects = relRepo.findManyTargets(task.getId(), "projects", new QuerySpec(Task.class));
		Assert.assertEquals(2, relProjects.size());

		relRepo.setRelations(task, Arrays.asList(project1.getId()), "projects");
		relProjects = relRepo.findManyTargets(task.getId(), "projects", new QuerySpec(Task.class));
		Assert.assertEquals(1, relProjects.size());
		Assert.assertEquals(project1.getId(), relProjects.get(0).getId());

		// FIXME HTTP DELETE method with payload not supported? at least in
		// Jersey
		// relRepo.removeRelations(task, Arrays.asList(project1.getId()),
		// "projects");
		// relProjects = relRepo.findManyTargets(task.getId(), "projects", new
		// QuerySpec());
		// Assert.assertEquals(0, relProjects.size());
	}
}