package io.katharsis.jpa.repository;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.JpaEntityRepository;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;

@Transactional
public abstract class JpaEntityRepositoryTestBase extends AbstractJpaTest {

	private JpaEntityRepository<TestEntity, Long> repo;
	private QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaEntityRepository<>(module, TestEntity.class);
		repo.setResourceRegistry(resourceRegistry);
	}

	@Test
	public void testGetEntityType() throws InstantiationException, IllegalAccessException {
		Assert.assertEquals(TestEntity.class, repo.getResourceClass());
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = repo.findAll(new QueryParams());
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
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "sort[test][longValue]", asc ? "asc" : "desc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		List<TestEntity> list = repo.findAll(queryParams);
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
		List<TestEntity> list = findAll("filter[test][stringValue]", "test1");
		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertEquals("test1", entity.getStringValue());
	}

	@Test
	public void testFilterLong() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][longValue]", "2");
		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertEquals(2, entity.getId().longValue());
		Assert.assertEquals(2L, entity.getLongValue());
	}

	@Test
	public void testFilterInt() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][embValue][embIntValue]", "2");
		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertEquals(2L, entity.getId().longValue());
		Assert.assertEquals(2, entity.getEmbValue().getEmbIntValue().intValue());
	}

	@Test
	public void testFilterBooleanTrue() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][embValue][nestedValue][embBoolValue]", "true");
		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertTrue(entity.getEmbValue().getNestedValue().getEmbBoolValue());
	}

	@Test
	public void testFilterBooleanFalse() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][embValue][nestedValue][embBoolValue]", "false");
		Assert.assertEquals(numTestEntities - 1, list.size());
		for (TestEntity entity : list) {
			Assert.assertFalse(entity.getEmbValue().getNestedValue().getEmbBoolValue());
		}
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][longValue][EQ]", "2");
		Assert.assertEquals(1, list.size());
	}

	@Test
	public void testFilterNotEquals() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][longValue][NEQ]", "2");
		Assert.assertEquals(4, list.size());
	}

	@Test
	public void testFilterLess() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][longValue][lt]", "2");
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void testFilterLessEqual() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][longValue][le]", "2");
		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][longValue][gt]", "1");
		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testFilterGreaterEqual() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][longValue][ge]", "1");
		Assert.assertEquals(4, list.size());
	}

	@Test
	public void testFilterLike() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][stringValue][like]", "test2");
		Assert.assertEquals(1, list.size());
	}

	@Test
	public void testFilterLikeWildcards() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("filter[test][stringValue][like]", "test%");
		Assert.assertEquals(5, list.size());
	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "page[offset]", "1");
		addParams(params, "page[limit]", "2");
		addParams(params, "sort[test][id]", "asc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		List<TestEntity> list = repo.findAll(queryParams);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(1, list.get(0).getId().intValue());
		Assert.assertEquals(2, list.get(1).getId().intValue());
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		List<TestEntity> list = findAll("include[test]", TestEntity.ATTR_oneRelatedValue);
		Assert.assertEquals(numTestEntities, list.size());
		for (TestEntity test : list) {
			assertTrue(Hibernate.isInitialized(test));
			assertTrue(Hibernate.isInitialized(test.getOneRelatedValue()));
		}
	}

	@Test
	public void testIncludeNoRelations() throws InstantiationException, IllegalAccessException {
		em.clear();
		List<TestEntity> list = repo.findAll(new QueryParams());
		Assert.assertEquals(numTestEntities, list.size());
		for (TestEntity entity : list) {
			RelatedEntity relatedValue = entity.getOneRelatedValue();
			if (relatedValue != null)
				Assert.assertFalse(Hibernate.isInitialized(relatedValue));
		}
	}

	@Test(expected = Exception.class)
	public void testIncludeRelationUnknownType() throws InstantiationException, IllegalAccessException {
		findAll("include[bla]", "test");
	}

	@Test(expected = Exception.class)
	public void testFilterUnknownType() throws InstantiationException, IllegalAccessException {
		findAll("filter[bla][stringValue]", "test%");
	}

	@Test(expected = Exception.class)
	public void testSparseFieldSetNotSupported() throws InstantiationException, IllegalAccessException {
		findAll("fields[test]", "name");
	}

	@Test(expected = Exception.class)
	public void testSortUnknownType() throws InstantiationException, IllegalAccessException {
		findAll("sort[bla][stringValue]", "asc");
	}

	private List<TestEntity> findAll(String paramKey, String paramValue) {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, paramKey, paramValue);
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		return repo.findAll(queryParams);
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}
}
