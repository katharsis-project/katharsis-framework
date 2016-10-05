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
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.jpa.util.EntityManagerProducer;
import io.katharsis.jpa.util.SpringTransactionRunner;
import io.katharsis.jpa.util.TestConfig;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;

public abstract class AbstractJpaJerseyTest extends JerseyTest {

	protected KatharsisClient client;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	protected AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString(), "io.katharsis.jpa.model.dto");

		JpaModule module = JpaModule.newClientModule(TestEntity.class.getPackage().getName());
		setupModule(module, false);
		client.addModule(module);

		client.getHttpClient().setReadTimeout(1000000, TimeUnit.MILLISECONDS);
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

			KatharsisFeature feature = new KatharsisFeature(new ObjectMapper(),
					new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator());

			JpaModule module = JpaModule.newServerModule(emFactory, em, transactionRunner);
			module.setQueryFactory(QuerydslQueryFactory.newInstance(module.getMetaLookup(), em));
			setupModule(module, true);
			feature.addModule(module);

			register(feature);

		}
	}

}
