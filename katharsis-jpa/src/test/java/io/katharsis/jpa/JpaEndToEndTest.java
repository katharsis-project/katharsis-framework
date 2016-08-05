package io.katharsis.jpa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.KatharsisClient;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.jpa.util.EntityManagerProducer;
import io.katharsis.jpa.util.SpringTransactionRunner;
import io.katharsis.jpa.util.TestConfig;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;

public class JpaEndToEndTest extends JerseyTest {

	private KatharsisClient client;
	private ResourceRepositoryStub<TestEntity, Long> testRepo;
	private ResourceRepositoryStub<RelatedEntity, Long> relatedRepo;

	private QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	private AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		client = new KatharsisClient(getBaseUri().toString(), "io.katharsis.client.mock");
		client.addModule(new JpaModule(TestEntity.class.getPackage().getName()));
		testRepo = client.getRepository(TestEntity.class);
		relatedRepo = client.getRepository(RelatedEntity.class);
		client.getHttpClient().setReadTimeout(1000000, TimeUnit.MILLISECONDS);
	}

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
			property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");

			context = new AnnotationConfigApplicationContext(TestConfig.class);
			context.start();
			EntityManagerFactory emFactory = context.getBean(EntityManagerFactory.class);
			EntityManager em = context.getBean(EntityManagerProducer.class).getEntityManager();
			SpringTransactionRunner transactionRunner = context.getBean(SpringTransactionRunner.class);

			KatharsisFeature feature = new KatharsisFeature(new ObjectMapper(),
					new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator());
			feature.addModule(new JpaModule(emFactory, em, transactionRunner));

			register(feature);

		}
	}

	
	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		List<TestEntity> list = findAll("include[test]", TestEntity.ATTR_oneRelatedValue);
		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			Assert.assertNotNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testIncludeNoRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			// in the future we may get proxies here
			Assert.assertNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testFindEmpty() {
		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testFindNull() {
		TestEntity test = testRepo.findOne(1L, new QueryParams());
		Assert.assertNull(test);
	}

	@Test
	public void testSaveAndFind() {
		TestEntity task = new TestEntity();
		task.setId(1L);
		task.setStringValue("test");
		testRepo.save(task);

		// check retrievable with findAll
		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertEquals(1, list.size());
		TestEntity savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findAll(ids)
		list = testRepo.findAll(Arrays.asList(1L), new QueryParams());
		Assert.assertEquals(1, list.size());
		savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findOne
		savedTask = testRepo.findOne(1L, new QueryParams());
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());
	}
	
	@Test
	public void testDelete() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.save(test);

		testRepo.delete(1L);

		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertEquals(0, list.size());
	}

	private QueryParams includeOneRelatedValueParams() {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[test]", TestEntity.ATTR_oneRelatedValue);
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		return queryParams;
	}

	@Test
	public void testSaveOneRelation() {
		TestEntity test = addTestWithOneRelation();

		TestEntity savedTest = testRepo.findOne(2L, includeOneRelatedValueParams());
		Assert.assertEquals(test.getId(), savedTest.getId());
		Assert.assertEquals(test.getStringValue(), savedTest.getStringValue());
		Assert.assertNotNull(savedTest.getOneRelatedValue());
		Assert.assertEquals(1L, savedTest.getOneRelatedValue().getId().longValue());
	}

	@Test
	public void testEagerOneRelation() {
		RelatedEntity related = new RelatedEntity();
		related.setId(1L);
		related.setStringValue("project");
		relatedRepo.save(related);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		test.setEagerRelatedValue(related);
		testRepo.save(test);
		
		TestEntity savedTest = testRepo.findOne(2L, new QueryParams());
		Assert.assertEquals(test.getId(), savedTest.getId());
		Assert.assertEquals(test.getStringValue(), savedTest.getStringValue());
		Assert.assertNull(savedTest.getOneRelatedValue());
		Assert.assertNotNull(savedTest.getEagerRelatedValue());
		Assert.assertEquals(1L, savedTest.getEagerRelatedValue().getId().longValue());
	}

	private TestEntity addTestWithOneRelation() {
		RelatedEntity related = new RelatedEntity();
		related.setId(1L);
		related.setStringValue("project");
		relatedRepo.save(related);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		test.setOneRelatedValue(related);
		testRepo.save(test, includeOneRelatedValueParams());
		return test;
	}

	private List<TestEntity> findAll(String paramKey, String paramValue) {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, paramKey, paramValue);
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		return testRepo.findAll(queryParams);
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}
}