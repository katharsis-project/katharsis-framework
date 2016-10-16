package io.katharsis.client;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.mock.repository.ProjectRepository;
import io.katharsis.client.mock.repository.TaskRepository;
import io.katharsis.client.mock.repository.TaskToProjectRepository;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;

public abstract class AbstractClientTest extends JerseyTest {

	protected KatharsisClient client;
	protected TestApplication testApplication;
	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString(), "io.katharsis.client.mock");
		client.addModule(new TestModule());

		TaskRepository.clear();
		ProjectRepository.clear();
		TaskToProjectRepository.clear();

		client.getHttpClient().setReadTimeout(1000000, TimeUnit.MILLISECONDS);
	}

	@Override
	protected TestApplication configure() {
		if (testApplication == null) {
			testApplication = new TestApplication(false);
		}
		
		return testApplication;
	}

	@ApplicationPath("/")
	public static class TestApplication extends ResourceConfig {

		private KatharsisTestFeature feature;

		public TestApplication(boolean querySpec) {
			property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.client.mock");
			property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");

			if (!querySpec) {
				feature = new KatharsisTestFeature(
					new ObjectMapper(), 
					new QueryParamsBuilder(new DefaultQueryParamsParser()),
					new SampleJsonServiceLocator());
			}
			else {
				feature = new KatharsisTestFeature(
					new ObjectMapper(), 
					new DefaultQuerySpecDeserializer(),
					new SampleJsonServiceLocator());
			}

			feature.addModule(new TestModule());

			register(feature);
		}

		public KatharsisFeature getFeature() {
			return feature;
		}
	}

	/**
	 * Assert the specified header name has the specified value.
	 * 
	 * @param name
	 * @param value
	 */
	protected void assertHasHeaderValue(String name, String value) {
		MultivaluedMap<String, String> headers = getLastReceivedHeaders();
		Assert.assertNotNull(headers);
		
		List<String> values = headers.get(name);
		Assert.assertNotNull(values);
		
		Assert.assertTrue(values.contains(value));
	}

	/**
	 * Clear the last received headers.
	 */
	protected void clearLastReceivedHeaders() {
		getTestFilter().clearLastReceivedHeaders();
	}

	/**
	 * Return the last received headers.
	 * 
	 * @return
	 */
	private MultivaluedMap<String, String> getLastReceivedHeaders() {
		return getTestFilter().getLastReceivedHeaders();
	}

	/**
	 * Return the configured test filter.
	 * 
	 * @return
	 */
	private TestRequestFilter getTestFilter() {
		return ((KatharsisTestFeature) testApplication.getFeature()).getTestFilter();
	}
}
