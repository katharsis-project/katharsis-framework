package io.katharsis.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.mock.models.Task;
import io.katharsis.client.module.TestException;
import io.katharsis.client.mock.models.Schedule;
import io.katharsis.client.mock.repository.ScheduleRepository;

public class ExceptionTest extends AbstractClientTest {

	protected ResourceRepositoryStub<Task, Long> taskRepo;

	@Before
	public void setup() {
		super.setup();
		taskRepo = client.getQueryParamsRepository(Task.class);
	}

	@Test
	public void genericRepo() {
		Task task = new Task();
		task.setId(10000L);
		task.setName("test");
		try {
			taskRepo.create(task);
			Assert.fail();
		}
		catch (TestException e) {
			Assert.assertEquals("msg", e.getMessage());
		}
	}
	
	@Test
	public void repoWithProxyAndInterface() {
		ScheduleRepository repo = client.getResourceRepository(ScheduleRepository.class);
		
		Schedule schedule = new Schedule();
		schedule.setId(10000L);
		schedule.setName("test");
		try {
			repo.create(schedule);
			Assert.fail();
		}
		catch (TestException e) {
			Assert.assertEquals("msg", e.getMessage());
		}
	}
}