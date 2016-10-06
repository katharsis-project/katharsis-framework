package io.katharsis.jpa.mapping;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

import io.katharsis.client.QuerySpecRelationshipRepositoryStub;
import io.katharsis.client.QuerySpecResourceRepositoryStub;
import io.katharsis.client.response.ResourceList;
import io.katharsis.jpa.AbstractJpaJerseyTest;
import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.model.QTestEntity;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.model.dto.RelatedDTO;
import io.katharsis.jpa.model.dto.TestDTO;
import io.katharsis.jpa.query.querydsl.QuerydslExpressionFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;

/**
 * Example of how to do DTO mapping and computed attributes.
 */
public class DtoMappingTest extends AbstractJpaJerseyTest {

	private QuerySpecResourceRepositoryStub<TestEntity, Long> testRepo;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getQuerySpecRepository(TestEntity.class);
	}

	@Test
	public void testReadAndUpdateFromEntity() throws InstantiationException, IllegalAccessException {
		// create as regular entity
		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.save(test);

		// query as regular entity (you may want to disable that in a real application)
		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());

		// query the mapped DTO
		QuerySpecResourceRepositoryStub<TestDTO, Serializable> dtoRepo = client.getQuerySpecRepository(TestDTO.class);
		List<TestDTO> dtos = dtoRepo.findAll(new QuerySpec(TestDTO.class));
		Assert.assertEquals(1, dtos.size());
		TestDTO dto = dtos.get(0);
		Assert.assertEquals(2L, dto.getId().longValue());
		Assert.assertEquals("test", dto.getStringValue());
		Assert.assertEquals("TEST", dto.getComputedUpperStringValue());

		// update the mapped dto
		dto.setStringValue("newValue");
		dtoRepo.save(dto);

		// read again
		dto = dtoRepo.findOne(2L, new QuerySpec(TestDTO.class));
		Assert.assertEquals(2L, dto.getId().longValue());
		Assert.assertEquals("newValue", dto.getStringValue());
		Assert.assertEquals("NEWVALUE", dto.getComputedUpperStringValue());

	}

	@Test
	public void testMappedRelation() {
		QuerySpecResourceRepositoryStub<TestDTO, Serializable> testRepo = client.getQuerySpecRepository(TestDTO.class);
		QuerySpecResourceRepositoryStub<RelatedDTO, Serializable> relatedRepo = client.getQuerySpecRepository(RelatedDTO.class);
		QuerySpecRelationshipRepositoryStub<TestDTO, Serializable, RelatedDTO, Serializable> relRepo = client
				.getQuerySpecRepository(TestDTO.class, RelatedDTO.class);

		TestDTO test = new TestDTO();
		test.setId(2L);
		test.setStringValue("createdDto");
		test = testRepo.save(test);

		RelatedDTO related = new RelatedDTO();
		related.setId(2L);
		related.setStringValue("createdDto");
		related = relatedRepo.save(related);

		relRepo.setRelation(test, related.getId(), TestEntity.ATTR_oneRelatedValue);

		// test relationship access
		RelatedDTO actualRelated = relRepo.findOneTarget(test.getId(), TestEntity.ATTR_oneRelatedValue,
				new QuerySpec(RelatedDTO.class));
		Assert.assertNotNull(actualRelated);
		Assert.assertEquals(related.getId(), actualRelated.getId());

		// test include
		QuerySpec querySpec = new QuerySpec(TestDTO.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		ResourceList<TestDTO> list = testRepo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		TestDTO actualTest = list.get(0);
		actualRelated = actualTest.getOneRelatedValue();
		Assert.assertNotNull(actualRelated);
		Assert.assertEquals(related.getId(), actualRelated.getId());
	}

	@Test
	public void testInsertDeleteDto() {
		QuerySpecResourceRepositoryStub<TestDTO, Serializable> dtoRepo = client.getQuerySpecRepository(TestDTO.class);

		// create a entity with a DTO and check properly saved
		TestDTO dto = new TestDTO();
		dto.setId(2L);
		dto.setStringValue("createdDto");
		dto = dtoRepo.save(dto);
		Assert.assertEquals("createdDto", dto.getStringValue());
		Assert.assertEquals("CREATEDDTO", dto.getComputedUpperStringValue());

		// check both exists
		ResourceList<TestDTO> dtos = dtoRepo.findAll(new QuerySpec(TestDTO.class));
		Assert.assertEquals(1, dtos.size());
		dto = dtos.get(0);
		Assert.assertEquals("createdDto", dto.getStringValue());
		Assert.assertEquals("CREATEDDTO", dto.getComputedUpperStringValue());

		// test delete
		dtoRepo.delete(dto.getId());
		dtos = dtoRepo.findAll(new QuerySpec(TestDTO.class));
		Assert.assertEquals(0, dtos.size());
	}

	@Test
	public void testSubQueryComputation() {
		QuerySpecResourceRepositoryStub<TestDTO, Serializable> dtoRepo = client.getQuerySpecRepository(TestDTO.class);

		int n = 5;
		for (long i = 0; i < n; i++) {
			TestDTO dto = new TestDTO();
			dto.setId(i + 100);
			dto.setStringValue(Long.toString(i));
			dtoRepo.save(dto);
		}

		// select, sort, filter by complex subquery
		QuerySpec querySpec = new QuerySpec(TestDTO.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList(TestDTO.ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS), FilterOperator.LT, 4));

		// TODO enable querySpec parser
		// querySpec.addSort(new SortSpec(Arrays.asList(TestDTO.ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS), Direction.DESC));

		ResourceList<TestDTO> dtos = dtoRepo.findAll(querySpec);
		Assert.assertEquals(4, dtos.size());
		for (int i = 0; i < dtos.size(); i++) {
			TestDTO dto = dtos.get(i);
			int j = i;// 4 - i;
			Assert.assertEquals(100 + j, dto.getId().longValue());
			Assert.assertEquals(j, dto.getComputedNumberOfSmallerIds());
		}
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);

		if (server) {
			EntityManager entityManager = module.getEntityManager();
			QuerydslExpressionFactory<QTestEntity> basicComputedValueFactory = new QuerydslExpressionFactory<QTestEntity>() {

				@Override
				public Expression<String> getExpression(QTestEntity parent, JPAQuery<?> jpaQuery) {
					return parent.stringValue.upper();
				}
			};
			QuerydslExpressionFactory<QTestEntity> complexComputedValueFactory = new QuerydslExpressionFactory<QTestEntity>() {

				@Override
				public Expression<Long> getExpression(QTestEntity parent, JPAQuery<?> jpaQuery) {
					QTestEntity root = QTestEntity.testEntity;
					QTestEntity sub = new QTestEntity("subquery");
					return JPAExpressions.select(sub.id.count()).from(sub).where(sub.id.lt(root.id));
				}
			};

			QuerydslQueryFactory queryFactory = (QuerydslQueryFactory) module.getQueryFactory();
			queryFactory.registerComputedAttribute(TestEntity.class, TestDTO.ATTR_COMPUTED_UPPER_STRING_VALUE, String.class,
					basicComputedValueFactory);
			queryFactory.registerComputedAttribute(TestEntity.class, TestDTO.ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS, Long.class,
					complexComputedValueFactory);
			module.addMappedEntityClass(TestEntity.class, TestDTO.class, new TestDTOMapper(entityManager));
			module.addMappedEntityClass(RelatedEntity.class, RelatedDTO.class, new RelatedDTOMapper(entityManager));
		}
	}
}