package io.katharsis.jpa;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.http.okhttp.OkHttpAdapter;
import io.katharsis.client.http.okhttp.OkHttpAdapterListenerBase;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.jpa.util.EntityManagerProducer;
import io.katharsis.jpa.util.SpringTransactionRunner;
import io.katharsis.jpa.util.TestConfig;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.rs.KatharsisFeature;
import okhttp3.OkHttpClient.Builder;

public abstract class AbstractJpaJerseyTest extends JerseyTest {

	protected KatharsisClient client;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	protected AnnotationConfigApplicationContext context;

	private boolean useQuerySpec = true;

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString());
		client.setPushAlways(false);

		JpaModule module = JpaModule.newClientModule();
		setupModule(module, false);
		client.addModule(module);
		setNetworkTimeout(client, 10000, TimeUnit.SECONDS);
	}

	public static void setNetworkTimeout(KatharsisClient client, final int timeout, final TimeUnit timeUnit) {
		OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
		httpAdapter.addListener(new OkHttpAdapterListenerBase() {

			@Override
			public void onBuild(Builder builder) {
				builder.readTimeout(timeout, timeUnit);
			}
		});
	}
	
	protected void setupModule(JpaModule module, boolean server) {
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();

		SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);
		transactionRunner.doInTransaction(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
				AbstractJpaTest.clear(em);
				return null;
			}
		});

		if (context != null)
			context.destroy();
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private class TestApplication extends ResourceConfig {

		public TestApplication() {
			property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.client.mock");

			Assert.assertNull(context);

			context = new AnnotationConfigApplicationContext(TestConfig.class);
			context.start();
			EntityManagerFactory emFactory = context.getBean(EntityManagerFactory.class);
			EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
			SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);

			KatharsisFeature feature;
			if (useQuerySpec) {
				feature = new KatharsisFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()),
						new SampleJsonServiceLocator());
			}
			else {
				feature = new KatharsisFeature(new ObjectMapper(), new DefaultQuerySpecDeserializer(),
						new SampleJsonServiceLocator());
			}

			JpaModule module = JpaModule.newServerModule(emFactory, em, transactionRunner);
			module.setQueryFactory(QuerydslQueryFactory.newInstance());
			setupModule(module, true);
			feature.addModule(module);

			register(feature);

		}
	}

}
