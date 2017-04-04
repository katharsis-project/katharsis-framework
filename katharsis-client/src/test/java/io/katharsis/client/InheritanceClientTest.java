package io.katharsis.client;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.internal.proxy.ObjectProxy;
import io.katharsis.client.mock.models.Project;
import io.katharsis.client.mock.models.Task;
import io.katharsis.client.mock.models.TaskSubType;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;

public class InheritanceClientTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	protected RelationshipRepositoryV2<Project, Long, Task, Long> relRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getRepositoryForType(Task.class);
		projectRepo = client.getRepositoryForType(Project.class);
		relRepo = client.getRepositoryForType(Project.class, Task.class);

		Task baseTask = new Task();
		baseTask.setId(Long.valueOf(1));
		baseTask.setName("baseTask");
		taskRepo.create(baseTask);

		TaskSubType taskSubType = new TaskSubType();
		taskSubType.setId(Long.valueOf(2));
		taskSubType.setName("taskSubType");
		taskSubType.setSubTypeValue(13);
		taskRepo.create(taskSubType);

		Project project = new Project();
		project.setId(1L);
		project.setName("project0");
		project.setTasks(Arrays.asList(baseTask, taskSubType));
		projectRepo.create(project);

		relRepo.addRelations(project, Arrays.asList(baseTask.getId(), taskSubType.getId()), "tasks");
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(true);
	}

	@Test
	public void testFindAll() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(2, tasks.size());

		Assert.assertEquals("baseTask", tasks.get(0).getName());
		Assert.assertEquals("taskSubType", tasks.get(1).getName());
	}

	@Test
	public void testIncludePoloymorphCollectionWithoutInclude() {
		doTestIncludePoloymorphCollection(false);
	}

	@Test
	public void testIncludePoloymorphCollectionWithInclude() {
		doTestIncludePoloymorphCollection(true);
	}

	private void doTestIncludePoloymorphCollection(boolean include) {
		QuerySpec querySpec = new QuerySpec(Project.class);
		if (include) {
			querySpec.includeRelation(Arrays.asList("tasks"));
		}
		List<Project> projects = projectRepo.findAll(querySpec);
		Assert.assertEquals(1, projects.size());
		Project project = projects.get(0);

		List<Task> tasks = project.getTasks();
		if (include) {
			Assert.assertFalse(tasks instanceof ObjectProxy);
		} else {
			ObjectProxy proxy = (ObjectProxy) tasks;
			Assert.assertFalse(proxy.isLoaded());
		}

		if (tasks.get(0).getName().equals("baseTask")) {
			Assert.assertEquals("baseTask", tasks.get(0).getName());
			Assert.assertEquals("taskSubType", tasks.get(1).getName());
		} else {
			Assert.assertEquals("baseTask", tasks.get(1).getName());
			Assert.assertEquals("taskSubType", tasks.get(0).getName());
		}
	}

}
