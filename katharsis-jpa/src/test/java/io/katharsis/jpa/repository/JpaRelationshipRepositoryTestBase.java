package io.katharsis.jpa.repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.JpaRelationshipRepository;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;

@Transactional
public abstract class JpaRelationshipRepositoryTestBase extends AbstractJpaTest {

	private JpaRelationshipRepository<TestEntity, Long, RelatedEntity, Long> repo;
	private JpaRelationshipRepository<RelatedEntity, Long, TestEntity, Long> relatedRepo;
	private QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaRelationshipRepository<TestEntity, Long, RelatedEntity, Long>(module, TestEntity.class,
				RelatedEntity.class);
		relatedRepo = new JpaRelationshipRepository<RelatedEntity, Long, TestEntity, Long>(module, RelatedEntity.class,
				TestEntity.class);
		repo.setResourceRegistry(resourceRegistry);
		relatedRepo.setResourceRegistry(resourceRegistry);
	}

	@Test
	public void testFindOneTarget() throws InstantiationException, IllegalAccessException {
		RelatedEntity relatedEntity = repo.findOneTarget(1L, TestEntity.ATTR_oneRelatedValue, new QueryParams());
		Assert.assertNotNull(relatedEntity);
		Assert.assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testFindManyTarget() throws InstantiationException, IllegalAccessException {
		Iterable<RelatedEntity> relatedEntities = repo.findManyTargets(1L, TestEntity.ATTR_oneRelatedValue,
				new QueryParams());
		Iterator<RelatedEntity> iterator = relatedEntities.iterator();
		RelatedEntity relatedEntity = iterator.next();
		Assert.assertFalse(iterator.hasNext());
		Assert.assertNotNull(relatedEntity);
		Assert.assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testFindManyTargetWithFilter() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[related][id]", "101");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		Iterable<RelatedEntity> relatedEntities = repo.findManyTargets(1L, TestEntity.ATTR_oneRelatedValue,
				queryParams);
		Iterator<RelatedEntity> iterator = relatedEntities.iterator();
		RelatedEntity relatedEntity = iterator.next();
		Assert.assertFalse(iterator.hasNext());
		Assert.assertNotNull(relatedEntity);
		Assert.assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testFindManyTargetWithUnmatchedFilter() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[related][id]", "9999");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		Iterable<RelatedEntity> relatedEntities = repo.findManyTargets(1L, TestEntity.ATTR_oneRelatedValue,
				queryParams);
		Iterator<RelatedEntity> iterator = relatedEntities.iterator();
		Assert.assertFalse(iterator.hasNext());
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}

	@Test
	public void testAddRemoveRelations() throws InstantiationException, IllegalAccessException {
		TestEntity test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(0, test.getManyRelatedValues().size());
		repo.addRelations(test, Arrays.asList(101L), TestEntity.ATTR_manyRelatedValues);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(1, test.getManyRelatedValues().size());
		RelatedEntity relatedEntity = test.getManyRelatedValues().iterator().next();
		Assert.assertEquals(101L, relatedEntity.getId().longValue());

		// add second relations
		repo.addRelations(test, Arrays.asList(102L), TestEntity.ATTR_manyRelatedValues);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(2, test.getManyRelatedValues().size());

		// remove relation
		repo.removeRelations(test, Arrays.asList(102L), TestEntity.ATTR_manyRelatedValues);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(1, test.getManyRelatedValues().size());

		// remove relation
		repo.removeRelations(test, Arrays.asList(101L), TestEntity.ATTR_manyRelatedValues);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(0, test.getManyRelatedValues().size());

		// set relations
		repo.setRelations(test, Arrays.asList(101L, 102L), TestEntity.ATTR_manyRelatedValues);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(2, test.getManyRelatedValues().size());

		// set relations
		repo.setRelations(test, Arrays.asList(101L), TestEntity.ATTR_manyRelatedValues);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(1, test.getManyRelatedValues().size());
		RelatedEntity related = test.getManyRelatedValues().iterator().next();
		Assert.assertEquals(101L, related.getId().longValue());
	}

	@Test
	public void testSetRelation() throws InstantiationException, IllegalAccessException {
		RelatedEntity related = em.find(RelatedEntity.class, 101L);
		TestEntity test = em.find(TestEntity.class, 1L);
		Assert.assertNull(related.getTestEntity());
		relatedRepo.setRelation(related, 1L, RelatedEntity.ATTR_testEntity);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(1, test.getManyRelatedValues().size());
		related = em.find(RelatedEntity.class, 101L);
		Assert.assertNotNull(related.getTestEntity());

		// test set null
		relatedRepo.setRelation(related, null, RelatedEntity.ATTR_testEntity);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertEquals(0, test.getManyRelatedValues().size());
		related = em.find(RelatedEntity.class, 101L);
		Assert.assertNull(related.getTestEntity());
	}
}
