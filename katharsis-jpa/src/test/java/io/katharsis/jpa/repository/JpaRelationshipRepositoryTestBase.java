package io.katharsis.jpa.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.JpaRelationshipRepository;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;

@Transactional
public abstract class JpaRelationshipRepositoryTestBase extends AbstractJpaTest {

	private JpaRelationshipRepository<TestEntity, Long, RelatedEntity, Long> repo;

	private JpaRelationshipRepository<RelatedEntity, Long, TestEntity, Long> relatedRepo;

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaRelationshipRepository<TestEntity, Long, RelatedEntity, Long>(module, TestEntity.class,
				RelatedEntity.class);
		relatedRepo = new JpaRelationshipRepository<RelatedEntity, Long, TestEntity, Long>(module, RelatedEntity.class,
				TestEntity.class);
	}

	@Test
	public void testFindOneTarget() throws InstantiationException, IllegalAccessException {
		RelatedEntity relatedEntity = repo.findOneTarget(1L, TestEntity.ATTR_oneRelatedValue, new QuerySpec(RelatedEntity.class));
		Assert.assertNotNull(relatedEntity);
		Assert.assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testFindManyTarget() throws InstantiationException, IllegalAccessException {
		Iterable<RelatedEntity> relatedEntities = repo.findManyTargets(1L, TestEntity.ATTR_oneRelatedValue,
				new QuerySpec(RelatedEntity.class));
		Iterator<RelatedEntity> iterator = relatedEntities.iterator();
		RelatedEntity relatedEntity = iterator.next();
		Assert.assertFalse(iterator.hasNext());
		Assert.assertNotNull(relatedEntity);
		Assert.assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testFindManyTargetWithFilter() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(RelatedEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 101));

		Iterable<RelatedEntity> relatedEntities = repo.findManyTargets(1L, TestEntity.ATTR_oneRelatedValue, querySpec);
		Iterator<RelatedEntity> iterator = relatedEntities.iterator();
		RelatedEntity relatedEntity = iterator.next();
		Assert.assertFalse(iterator.hasNext());
		Assert.assertNotNull(relatedEntity);
		Assert.assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testFindManyTargetWithUnmatchedFilter() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(RelatedEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 9999));

		Iterable<RelatedEntity> relatedEntities = repo.findManyTargets(1L, TestEntity.ATTR_oneRelatedValue, querySpec);
		Iterator<RelatedEntity> iterator = relatedEntities.iterator();
		Assert.assertFalse(iterator.hasNext());
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

	@Test
	public void testGetManyRelation() {
		TestEntity test = em.find(TestEntity.class, 1L);
		Assert.assertThat(test.getManyRelatedValues().size(), Is.is(0));

		repo.addRelations(test, Arrays.asList(101L,102L), TestEntity.ATTR_manyRelatedValues);
		em.flush();
		em.clear();
		test = em.find(TestEntity.class, 1L);
		Assert.assertThat(test.getManyRelatedValues().size(), Is.is(2));

		QuerySpec querySpec = new QuerySpec(RelatedEntity.class);
		Iterable<RelatedEntity> targets = repo.findManyTargets(1L, TestEntity.ATTR_manyRelatedValues, querySpec);
		List<RelatedEntity> res = new ArrayList<>();
		for (RelatedEntity relatedEntity : targets) {
			res.add(relatedEntity);
		}
		Assert.assertThat(res.size(), Is.is(2));
		Assert.assertThat(res.get(0).getId(), Is.is(101L));
		Assert.assertThat(res.get(1).getId(), Is.is(102L));
	}
}
