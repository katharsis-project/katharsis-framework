package io.katharsis.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.mock.models.Schedule;
import io.katharsis.client.mock.repository.ScheduleRepository;
import io.katharsis.queryspec.QuerySpec;

public class ActionTest extends AbstractClientTest {

	protected ScheduleRepository scheduleRepo;

	@Before
	public void setup() {
		super.setup();
		scheduleRepo = client.getResourceRepository(ScheduleRepository.class);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(true);
	}

	@Test
	public void testCrudFind() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("schedule");
		scheduleRepo.save(schedule);

		Iterable<Schedule> schedules = scheduleRepo.findAll(new QuerySpec(Schedule.class));
		schedule = schedules.iterator().next();
		Assert.assertEquals("schedule", schedule.getName());
		
		scheduleRepo.delete(schedule.getId());
		schedules = scheduleRepo.findAll(new QuerySpec(Schedule.class));
		Assert.assertFalse(schedules.iterator().hasNext());
	}
	
	@Test
	public void testInvokeAction() {
		String result = scheduleRepo.repositoryAction("hello");
		Assert.assertEquals("repository action: hello", result);
	}
}