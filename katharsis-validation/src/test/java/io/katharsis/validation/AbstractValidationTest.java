package io.katharsis.validation;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.RelationshipRepositoryStub;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.validation.mock.models.Project;
import io.katharsis.validation.mock.models.Task;
import io.katharsis.validation.mock.repository.TaskRepository;

public abstract class AbstractValidationTest extends JerseyTest {

	protected KatharsisClient client;

	protected ResourceRepositoryStub<Task, Long> taskRepo;

	protected ResourceRepositoryStub<Project, Long> projectRepo;

	protected RelationshipRepositoryStub<Task, Long, Project, Long> relRepo;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString());
		client.addModule(ValidationModule.newInstance());
		taskRepo = client.getQueryParamsRepository(Task.class);
		projectRepo = client.getQueryParamsRepository(Project.class);
		relRepo = client.getQueryParamsRepository(Task.class, Project.class);
		TaskRepository.map.clear();
		
		client.getHttpAdapter().setReceiveTimeout(1000000, TimeUnit.MILLISECONDS);
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private static class TestApplication extends ResourceConfig {

		public TestApplication() {
			property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, getClass().getPackage().getName());
			property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");

			KatharsisFeature feature = new KatharsisFeature(new ObjectMapper(),
					new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator());
			feature.addModule(ValidationModule.newInstance());
			register(feature);

		}
	}
}
