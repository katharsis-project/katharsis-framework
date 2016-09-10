package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.persistence.OptimisticLockException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEmbeddedIdEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.model.TestIdEmbeddable;
import io.katharsis.jpa.model.VersionedEntity;
import io.katharsis.queryspec.QuerySpec;

public class JpaQuerySpecEndToEndTest extends AbstractJpaJerseyTest {

	private QuerySpecResourceRepositoryStub<TestEntity, Long> testRepo;

	@Before
	public void setup() {
		super.setup();
		testRepo = client.getQuerySpecRepository(TestEntity.class);
	}
	
	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();
		
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			Assert.assertNotNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testIncludeNoRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			// in the future we may get proxies here
			Assert.assertNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testFindEmpty() {
		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testFindNull() {
		TestEntity test = testRepo.findOne(1L, new QuerySpec(TestEntity.class));
		Assert.assertNull(test);
	}

	@Test
	public void testSaveAndFind() {
		TestEntity task = new TestEntity();
		task.setId(1L);
		task.setStringValue("test");
		testRepo.save(task);

		// check retrievable with findAll
		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		TestEntity savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findAll(ids)
		list = testRepo.findAll(Arrays.asList(1L), new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findOne
		savedTask = testRepo.findOne(1L, new QuerySpec(TestEntity.class));
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());
	}

	@Test
	public void testOptimisticLocking() {
		QuerySpecResourceRepositoryStub<VersionedEntity, Serializable> repo = client.getQuerySpecRepository(VersionedEntity.class);
		VersionedEntity entity = new VersionedEntity();
		entity.setId(1L);
		entity.setLongValue(13L);
		VersionedEntity saved = repo.save(entity);
		Assert.assertEquals(0, saved.getVersion());

		saved.setLongValue(14L);
		saved = repo.save(saved);
		Assert.assertEquals(1, saved.getVersion());

		saved.setLongValue(15L);
		saved = repo.save(saved);
		Assert.assertEquals(2, saved.getVersion());

		saved.setLongValue(16L);
		saved.setVersion(saved.getVersion() - 1);
		try {
			saved = repo.save(saved);
			Assert.fail();
		}
		catch (OptimisticLockException e) {
			// ok
		}

		VersionedEntity persisted = repo.findOne(1L, new QuerySpec(VersionedEntity.class));
		Assert.assertEquals(2, persisted.getVersion());
		Assert.assertEquals(15L, persisted.getLongValue());
	}

	@Test
	public void testDelete() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.save(test);

		testRepo.delete(1L);

		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(0, list.size());
	}

	private QuerySpec includeOneRelatedValueParams() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		return querySpec;
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
		ResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getRepository(RelatedEntity.class);
		RelatedEntity related = new RelatedEntity();
		related.setId(1L);
		related.setStringValue("project");
		relatedRepo.save(related);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		test.setEagerRelatedValue(related);
		testRepo.save(test);

		TestEntity savedTest = testRepo.findOne(2L, new QuerySpec(TestEntity.class));
		Assert.assertEquals(test.getId(), savedTest.getId());
		Assert.assertEquals(test.getStringValue(), savedTest.getStringValue());
		Assert.assertNull(savedTest.getOneRelatedValue());
		Assert.assertNotNull(savedTest.getEagerRelatedValue());
		Assert.assertEquals(1L, savedTest.getEagerRelatedValue().getId().longValue());
	}

	@Test
	public void testEmbeddableIds() throws InstantiationException, IllegalAccessException {
		QuerySpecResourceRepositoryStub<TestEmbeddedIdEntity, Serializable> rep = client.getQuerySpecRepository(TestEmbeddedIdEntity.class);

		// add
		TestEmbeddedIdEntity entity = new TestEmbeddedIdEntity();
		entity.setId(new TestIdEmbeddable(13, "test"));
		entity.setLongValue(100L);
		rep.save(entity);

		List<TestEmbeddedIdEntity> list = rep.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		TestEmbeddedIdEntity savedEntity = list.get(0);
		Assert.assertNotNull(savedEntity);
		Assert.assertEquals(100L, savedEntity.getLongValue());
		Assert.assertEquals(13, savedEntity.getId().getEmbIntValue().intValue());
		Assert.assertEquals("test", savedEntity.getId().getEmbStringValue());

		// update
		savedEntity.setLongValue(101L);
		rep.save(savedEntity);
		list = rep.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		savedEntity = list.get(0);
		Assert.assertEquals(101L, savedEntity.getLongValue());

		// delete
		rep.delete(entity.getId());
		list = rep.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(0, list.size());
	}

	private TestEntity addTestWithOneRelation() {
		QuerySpecResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);
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
}