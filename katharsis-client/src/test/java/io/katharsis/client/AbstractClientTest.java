package io.katharsis.client;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.mock.models.Project;
import io.katharsis.client.mock.models.Task;
import io.katharsis.client.mock.repository.TaskRepository;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;

public class AbstractClientTest extends JerseyTest {

	protected KatharsisClient client;
	protected ResourceRepositoryStub<Task, Long> taskRepo;
	protected ResourceRepositoryStub<Project, Long> projectRepo;
	protected RelationshipRepositoryStub<Task, Long, Project, Long> relRepo;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString(), "io.katharsis.client.mock");
		client.addModule(new TestModule());
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
			
			KatharsisFeature feature = new KatharsisFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()),
					new SampleJsonServiceLocator());
			
			feature.addModule(new TestModule());
			
			register(feature);

		}
	}
}
