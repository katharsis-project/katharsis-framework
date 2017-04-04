package io.katharsis.client.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.bridge.SLF4JBridgeHandler;

import io.katharsis.client.AbstractClientTest;
import io.katharsis.client.KatharsisTestFeature;
import io.katharsis.client.mock.models.Schedule;
import io.katharsis.client.mock.repository.ScheduleRepository;
import io.katharsis.core.internal.dispatcher.path.ActionPath;
import io.katharsis.module.SimpleModule;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.filter.DocumentFilter;
import io.katharsis.repository.filter.DocumentFilterChain;
import io.katharsis.repository.filter.DocumentFilterContext;
import io.katharsis.repository.response.Response;

public class BasicActionTest extends AbstractClientTest {

	protected ScheduleRepository scheduleRepo;

	private DocumentFilter filter;

	@Before
	public void setup() {
		SLF4JBridgeHandler.install();
		super.setup();
		scheduleRepo = client.getRepositoryForInterface(ScheduleRepository.class);
	}

	@Override
	protected void setupFeature(KatharsisTestFeature feature) {
		filter = Mockito.spy(new DocumentFilter() {

			@Override
			public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
				return chain.doFilter(filterRequestContext);
			}
		});
		SimpleModule testModule = new SimpleModule("testFilter");
		testModule.addFilter(filter);
		feature.addModule(testModule);
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
		scheduleRepo.create(schedule);

		Iterable<Schedule> schedules = scheduleRepo.findAll(new QuerySpec(Schedule.class));
		schedule = schedules.iterator().next();
		Assert.assertEquals("schedule", schedule.getName());

		scheduleRepo.delete(schedule.getId());
		schedules = scheduleRepo.findAll(new QuerySpec(Schedule.class));
		Assert.assertFalse(schedules.iterator().hasNext());
	}

	@Test
	public void testInvokeRepositoryAction() {
		String result = scheduleRepo.repositoryAction("hello");
		Assert.assertEquals("repository action: hello", result);

		// check filters
		ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
		Mockito.verify(filter, Mockito.times(1)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));
		DocumentFilterContext actionContext = contexts.getAllValues().get(0);
		Assert.assertEquals("GET", actionContext.getMethod());
		Assert.assertTrue(actionContext.getJsonPath() instanceof ActionPath);
	}

	@Test
	public void testInvokeResourceAction() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("scheduleName");
		scheduleRepo.create(schedule);

		String result = scheduleRepo.resourceAction(1, "hello");
		Assert.assertEquals("resource action: hello@scheduleName", result);

		// check filters
		ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
		Mockito.verify(filter, Mockito.times(2)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));
		DocumentFilterContext actionContext = contexts.getAllValues().get(1);
		Assert.assertEquals("GET", actionContext.getMethod());
		Assert.assertTrue(actionContext.getJsonPath() instanceof ActionPath);
	}
}