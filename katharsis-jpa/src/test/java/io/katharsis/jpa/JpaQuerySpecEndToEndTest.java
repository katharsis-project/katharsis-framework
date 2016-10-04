package io.katharsis.jpa;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.persistence.OptimisticLockException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.katharsis.client.QuerySpecRelationshipRepositoryStub;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.ResourceRepositoryStub;
import io.katharsis.client.response.JsonLinksInformation;
import io.katharsis.client.response.JsonMetaInformation;
import io.katharsis.client.response.ResourceList;
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
	public void testFindOneTargetWithNullResult() throws InstantiationException, IllegalAccessException {
		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.save(test);

		QuerySpecRelationshipRepositoryStub<TestEntity, Serializable, RelatedEntity, Serializable> relRepo = client
				.getQuerySpecRepository(TestEntity.class, RelatedEntity.class);

		RelatedEntity related = relRepo.findOneTarget(test.getId(), TestEntity.ATTR_oneRelatedValue,
				new QuerySpec(RelatedEntity.class));
		Assert.assertNull(related);
	}

	@Test
	public void testFindOneTarget() throws InstantiationException, IllegalAccessException {
		TestEntity test = addTestWithOneRelation();

		QuerySpecRelationshipRepositoryStub<TestEntity, Serializable, RelatedEntity, Serializable> relRepo = client
				.getQuerySpecRepository(TestEntity.class, RelatedEntity.class);

		RelatedEntity related = relRepo.findOneTarget(test.getId(), TestEntity.ATTR_oneRelatedValue,
				new QuerySpec(RelatedEntity.class));
		Assert.assertNotNull(related);
	}

	@Test
	public void testAddManyRelationWithRelationshipRepository() throws InstantiationException, IllegalAccessException {
		testAddManyRelation(false);
	}

	@Test
	@Ignore
	// TODO bidirectionality not properly handled, see
	// ResourceUpsert should make use of relationship repositories #130
	public void testAddManyRelationWithResourceSave() throws InstantiationException, IllegalAccessException {
		testAddManyRelation(true);
	}

	private void testAddManyRelation(boolean onSave) throws InstantiationException, IllegalAccessException {
		QuerySpecResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);
		RelatedEntity related1 = new RelatedEntity();
		related1.setId(1L);
		related1.setStringValue("related1");
		relatedRepo.save(related1);

		RelatedEntity related2 = new RelatedEntity();
		related2.setId(2L);
		related2.setStringValue("related2");
		relatedRepo.save(related2);

		TestEntity test = new TestEntity();
		test.setId(3L);
		test.setStringValue("test");
		if (onSave) {
			test.setManyRelatedValues(Arrays.asList(related1, related2));
		}
		testRepo.save(test, includeManyRelatedValueParams());

		// query relation
		QuerySpecRelationshipRepositoryStub<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getQuerySpecRepository(TestEntity.class, RelatedEntity.class);
		if (!onSave) {
			relRepo.addRelations(test, Arrays.asList(1L, 2L), TestEntity.ATTR_manyRelatedValues);
		}
		List<RelatedEntity> related = relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues,
				new QuerySpec(RelatedEntity.class));
		Assert.assertEquals(2, related.size());

		// query relation in opposite direction
		QuerySpecRelationshipRepositoryStub<RelatedEntity, Serializable, TestEntity, Serializable> backRelRepo = client
				.getQuerySpecRepository(RelatedEntity.class, TestEntity.class);
		test = backRelRepo.findOneTarget(2L, RelatedEntity.ATTR_testEntity, new QuerySpec(TestEntity.class));
		Assert.assertNotNull(test);
		Assert.assertEquals(3L, test.getId().longValue());
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
	public void testRootPaging() {
		for (long i = 0; i < 5; i++) {
			TestEntity task = new TestEntity();
			task.setId(i);
			task.setStringValue("test");
			testRepo.save(task);
		}

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<TestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		JsonMetaInformation meta = list.getMetaInformation(JsonMetaInformation.class);
		JsonLinksInformation links = list.getLinksInformation(JsonLinksInformation.class);
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);

		String baseUri = getBaseUri().toString();
		Assert.assertEquals(baseUri + "test/?page[limit]=2", links.asJsonNode().get("first").asText());
		Assert.assertEquals(baseUri + "test/?page[limit]=2&page[offset]=4", links.asJsonNode().get("last").asText());
		Assert.assertEquals(baseUri + "test/?page[limit]=2", links.asJsonNode().get("prev").asText());
		Assert.assertEquals(baseUri + "test/?page[limit]=2&page[offset]=4", links.asJsonNode().get("next").asText());
	}
	
	@Test
	public void testRelationPaging() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.save(test);

		ResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getRepository(RelatedEntity.class);
		QuerySpecRelationshipRepositoryStub<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getQuerySpecRepository(TestEntity.class, RelatedEntity.class);
		for(long i = 0; i < 5;i++){
			RelatedEntity related1 = new RelatedEntity();
			related1.setId(i);
			related1.setStringValue("related" + i);
			relatedRepo.save(related1);
			
			relRepo.addRelations(test, Arrays.asList(i), TestEntity.ATTR_manyRelatedValues);
		}

		QuerySpec querySpec = new QuerySpec(RelatedEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<RelatedEntity> list = relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues, querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		JsonMetaInformation meta = list.getMetaInformation(JsonMetaInformation.class);
		JsonLinksInformation links = list.getLinksInformation(JsonLinksInformation.class);
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);

		String baseUri = getBaseUri().toString();
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues/?page[limit]=2", links.asJsonNode().get("first").asText());
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues/?page[limit]=2&page[offset]=4", links.asJsonNode().get("last").asText());
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues/?page[limit]=2", links.asJsonNode().get("prev").asText());
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues/?page[limit]=2&page[offset]=4", links.asJsonNode().get("next").asText());
	}

	@Test
	public void testOptimisticLocking() {
		QuerySpecResourceRepositoryStub<VersionedEntity, Serializable> repo = client
				.getQuerySpecRepository(VersionedEntity.class);
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

	private QuerySpec includeManyRelatedValueParams() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues));
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
		QuerySpecResourceRepositoryStub<TestEmbeddedIdEntity, Serializable> rep = client
				.getQuerySpecRepository(TestEmbeddedIdEntity.class);

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