package io.katharsis.jpa.repository;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Hibernate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.JpaEntityRepository;
import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.SequenceEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.meta.PagedMetaInformation;

@Transactional
public abstract class JpaEntityRepositoryTestBase extends AbstractJpaTest {

	protected JpaEntityRepository<TestEntity, Long> repo;

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaEntityRepository<>(module, JpaRepositoryConfig.create(TestEntity.class));
	}

	@Test
	public void testGetEntityType() throws InstantiationException, IllegalAccessException {
		Assert.assertEquals(TestEntity.class, repo.getResourceClass());
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);

		List<TestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(numTestEntities, list.size());
	}

	@Test
	public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
		testFindAllOrder(true);
	}

	@Test
	public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
		testFindAllOrder(false);
	}

	public void testFindAllOrder(boolean asc) throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addSort(new SortSpec(Arrays.asList("longValue"), asc ? Direction.ASC : Direction.DESC));
		List<TestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(numTestEntities, list.size());
		for (int i = 0; i < numTestEntities; i++) {
			if (asc) {
				Assert.assertEquals(i, list.get(i).getLongValue());
			} else {
				Assert.assertEquals(numTestEntities - 1 - i, list.get(i).getLongValue());
			}
		}
	}

	@Test
	public void testFilterString() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.EQ, "test1"));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertEquals("test1", entity.getStringValue());
	}

	@Test
	public void testFilterLong() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.EQ, 2L));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertEquals(2, entity.getId().longValue());
		Assert.assertEquals(2L, entity.getLongValue());
	}

	@Test
	public void testFilterInt() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("embValue", "embIntValue"), FilterOperator.EQ, 2));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertEquals(2L, entity.getId().longValue());
		Assert.assertEquals(2, entity.getEmbValue().getEmbIntValue().intValue());
	}

	@Test
	public void testFilterBooleanTrue() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(
				new FilterSpec(Arrays.asList("embValue", "nestedValue", "embBoolValue"), FilterOperator.EQ, true));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertTrue(entity.getEmbValue().getNestedValue().getEmbBoolValue());
	}

	@Test
	public void testFilterBooleanFalse() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(
				new FilterSpec(Arrays.asList("embValue", "nestedValue", "embBoolValue"), FilterOperator.EQ, false));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(numTestEntities - 1, list.size());
		for (TestEntity entity : list) {
			Assert.assertFalse(entity.getEmbValue().getNestedValue().getEmbBoolValue());
		}
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.EQ, 2L));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
	}

	@Test
	public void testFilterNotEquals() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.NEQ, 2L));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(4, list.size());
	}

	@Test
	public void testFilterLess() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.LT, 2));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(2, list.size());
	}

	@Test
	public void testFilterLessEqual() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.LE, 2));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.GT, 1));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testFilterGreaterEqual() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.GE, 1));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(4, list.size());
	}

	@Test
	public void testFilterLike() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.LIKE, "test2"));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
	}

	@Test
	public void testFilterLikeWildcards() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.LIKE, "test%"));
		List<TestEntity> list = repo.findAll(querySpec);

		Assert.assertEquals(5, list.size());
	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assert.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}

	@Test
	public void testPagingFirst() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(0L);
		querySpec.setLimit(3L);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(0, list.get(0).getId().intValue());
		Assert.assertEquals(1, list.get(1).getId().intValue());
		Assert.assertEquals(2, list.get(2).getId().intValue());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assert.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}

	@Test
	public void testPagingLast() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(4L);
		querySpec.setLimit(4L);

		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(4, list.get(0).getId().intValue());

		PagedMetaInformation metaInformation = list.getMeta(PagedMetaInformation.class);
		Assert.assertEquals(5, metaInformation.getTotalResourceCount().longValue());
	}

	@Test
	public void testIncludeNoRelations() throws InstantiationException, IllegalAccessException {
		em.clear();
		List<TestEntity> list = repo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(numTestEntities, list.size());
		for (TestEntity entity : list) {
			RelatedEntity relatedValue = entity.getOneRelatedValue();
			if (relatedValue != null)
				Assert.assertFalse(Hibernate.isInitialized(relatedValue));
		}
	}

	@Test(expected = Exception.class)
	public void testFilterUnknownAttr() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("test"), FilterOperator.EQ, "test"));
		repo.findAll(querySpec);
	}

	@Test(expected = Exception.class)
	public void testSparseFieldSetNotSupported() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeField(Arrays.asList("test"));
		repo.findAll(querySpec);
	}

	@Test(expected = Exception.class)
	public void testSortUnknownAttr() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addSort(new SortSpec(Arrays.asList("test"), Direction.DESC));
		repo.findAll(querySpec);
	}

	@Test
	public void testSequencePrimaryKey() throws InstantiationException, IllegalAccessException {
		JpaEntityRepository<SequenceEntity, Long> sequenceRepo = new JpaEntityRepository<>(module,
				JpaRepositoryConfig.create(SequenceEntity.class));
		QuerySpec querySpec = new QuerySpec(SequenceEntity.class);
		List<SequenceEntity> list = sequenceRepo.findAll(querySpec);
		Assert.assertEquals(0, list.size());

		SequenceEntity entity = new SequenceEntity();
		entity.setStringValue("someValue");
		entity = sequenceRepo.create(entity);

		Assert.assertNotNull(entity.getId());
		Assert.assertNotEquals(0L, entity.getId().longValue());

		entity.setStringValue("someUpdatedValue");
		entity = sequenceRepo.save(entity);
		Assert.assertEquals("someUpdatedValue", entity.getStringValue());
	}
}
