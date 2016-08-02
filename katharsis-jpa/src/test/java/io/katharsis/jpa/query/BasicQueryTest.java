package io.katharsis.jpa.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.JoinType;

import org.hibernate.Hibernate;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.internal.query.FilterOperator;
import io.katharsis.jpa.internal.query.FilterSpec;
import io.katharsis.jpa.internal.query.OrderSpec.Direction;
import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.jpa.internal.query.QueryExecutor;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;

@Transactional
public class BasicQueryTest extends AbstractJpaTest {

	private QueryBuilder<TestEntity> builder() {
		return factory.newBuilder(TestEntity.class);
	}

	@Test
	public void testAll() {
		assertEquals(5, builder().buildExecutor().getResultList().size());
	}

	@Test
	public void testRelations() {
		List<Long> ids = Arrays.asList(1L);
		QueryBuilder<RelatedEntity> builder = factory.newBuilder(TestEntity.class, TestEntity.ATTR_oneRelatedValue,
				ids);
		RelatedEntity relatedEntity = builder.buildExecutor().getUniqueResult(false);
		assertEquals(101L, relatedEntity.getId().longValue());
	}

	@Test
	public void testEqualsFilter() {
		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQUAL, 0L).buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQUAL, 1L).buildExecutor()
				.getUniqueResult(false).getId());
		assertEquals((Long) 2L, builder().addFilter(TestEntity.ATTR_id, FilterOperator.EQUAL, 2L).buildExecutor()
				.getUniqueResult(false).getId());

		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.EQUAL, "test0")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.EQUAL, "test1")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 2L, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.EQUAL, "test2")
				.buildExecutor().getUniqueResult(false).getId());
	}

	@Test
	public void testNotEqualsFilter() {
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.NOT_EQUAL, 0L).buildExecutor()
				.getResultList().size());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.NOT_EQUAL, 1L).buildExecutor()
				.getResultList().size());
		assertEquals(5, builder().addFilter(TestEntity.ATTR_id, FilterOperator.NOT_EQUAL, 9999L).buildExecutor()
				.getResultList().size());
	}

	@Test
	public void testLikeFilter() {
		assertEquals(5, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LIKE, "test%").buildExecutor()
				.getResultList().size());
		assertEquals(1, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LIKE, "test1").buildExecutor()
				.getResultList().size());
		assertEquals(0, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.LIKE, "abc").buildExecutor()
				.getResultList().size());
	}

	@Test
	public void testNotLikeFilter() {
		assertEquals(0, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.NOT_LIKE, "test%")
				.buildExecutor().getResultList().size());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.NOT_LIKE, "test1")
				.buildExecutor().getResultList().size());
		assertEquals(5, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.NOT_LIKE, "abc").buildExecutor()
				.getResultList().size());

	}

	@Test
	public void testGreaterFilter() {
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GREATER, 0L).buildExecutor()
				.getResultList().size());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GREATER, 1L).buildExecutor()
				.getResultList().size());
		assertEquals(0, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GREATER, 4L).buildExecutor()
				.getResultList().size());
	}

	@Test
	public void testLessFilter() {
		assertEquals(0, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LESS, 0L).buildExecutor().getResultList()
				.size());
		assertEquals(1, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LESS, 1L).buildExecutor().getResultList()
				.size());
		assertEquals(2, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LESS, 2L).buildExecutor().getResultList()
				.size());
	}

	@Test
	public void testGreaterEqualsFilter() {
		assertEquals(5, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 0L).buildExecutor()
				.getResultList().size());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 1L).buildExecutor()
				.getResultList().size());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 2L).buildExecutor()
				.getResultList().size());
	}

	@Test
	public void testLessEqualsFilter() {
		assertEquals(1, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LESS_EQUAL, 0L).buildExecutor()
				.getResultList().size());
		assertEquals(2, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LESS_EQUAL, 1L).buildExecutor()
				.getResultList().size());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_id, FilterOperator.LESS_EQUAL, 2L).buildExecutor()
				.getResultList().size());
	}

	@Test
	public void testILikeFilter() {
		assertEquals(1, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.ILIKE, "test1").buildExecutor()
				.getResultList().size());
		assertEquals(1, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.ILIKE, "tEst1").buildExecutor()
				.getResultList().size());
		assertEquals(1, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.ILIKE, "TEst1").buildExecutor()
				.getResultList().size());
		assertEquals(5, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.ILIKE, "TEst%").buildExecutor()
				.getResultList().size());
		assertEquals(5, builder().addFilter(TestEntity.ATTR_stringValue, FilterOperator.ILIKE, "test%").buildExecutor()
				.getResultList().size());
	}

	@Test
	public void testAndFilter() {
		assertEquals(4,
				builder()
						.addFilter(FilterSpec.and(new FilterSpec(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 0L),
								new FilterSpec(TestEntity.ATTR_id, FilterOperator.LESS, 4L)))
						.buildExecutor().getResultList().size());
		assertEquals(1,
				builder()
						.addFilter(FilterSpec.and(new FilterSpec(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 3L),
								new FilterSpec(TestEntity.ATTR_id, FilterOperator.LESS, 4L)))
						.buildExecutor().getResultList().size());
		assertEquals(0,
				builder()
						.addFilter(FilterSpec.and(new FilterSpec(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 3L),
								new FilterSpec(TestEntity.ATTR_id, FilterOperator.LESS, 3L)))
						.buildExecutor().getResultList().size());
	}

	@Test
	public void testNotFilter() {
		assertEquals(5,
				builder()
						.addFilter(FilterSpec.not(new FilterSpec(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 5L)))
						.buildExecutor().getResultList().size());
		assertEquals(3,
				builder()
						.addFilter(FilterSpec.not(new FilterSpec(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 3L)))
						.buildExecutor().getResultList().size());
	}

	@Test
	public void testOrFilter() {
		assertEquals(5,
				builder()
						.addFilter(FilterSpec.or(new FilterSpec(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 3L),
								new FilterSpec(TestEntity.ATTR_id, FilterOperator.LESS, 3L)))
						.buildExecutor().getResultList().size());
		assertEquals(2,
				builder()
						.addFilter(FilterSpec.or(new FilterSpec(TestEntity.ATTR_id, FilterOperator.GREATER_EQUAL, 4L),
								new FilterSpec(TestEntity.ATTR_id, FilterOperator.LESS, 1L)))
						.buildExecutor().getResultList().size());
	}

	@Test
	public void testEmbeddableFilter() {
		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_embValue_intValue, FilterOperator.EQUAL, 0)
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_embValue_intValue, FilterOperator.EQUAL, 1)
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 2L, builder().addFilter(TestEntity.ATTR_embValue_intValue, FilterOperator.EQUAL, 2)
				.buildExecutor().getUniqueResult(false).getId());

		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_embValue_stringValue, FilterOperator.EQUAL, "emb0")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_embValue_stringValue, FilterOperator.EQUAL, "emb1")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 2L, builder().addFilter(TestEntity.ATTR_embValue_stringValue, FilterOperator.EQUAL, "emb2")
				.buildExecutor().getUniqueResult(false).getId());

		assertEquals((Long) 0L,
				builder().addFilter(TestEntity.ATTR_embValue_nestedValue_boolValue, FilterOperator.EQUAL, true)
						.buildExecutor().getUniqueResult(false).getId());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_embValue_nestedValue_boolValue, FilterOperator.EQUAL, false)
				.buildExecutor().getResultList().size());
	}

	@Test
	public void testMapFilter() {
		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_mapValue + ".a", FilterOperator.EQUAL, "a0")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 0L, builder().addFilter(TestEntity.ATTR_mapValue + ".b", FilterOperator.EQUAL, "b0")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_mapValue + ".a", FilterOperator.EQUAL, "a1")
				.buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L, builder().addFilter(TestEntity.ATTR_mapValue + ".b", FilterOperator.EQUAL, "b1")
				.buildExecutor().getUniqueResult(false).getId());
		assertNull(builder().addFilter(TestEntity.ATTR_mapValue + ".a", FilterOperator.EQUAL, "b1").buildExecutor()
				.getUniqueResult(true));
	}

	@Test
	public void testJoinFilter() {
		assertEquals((Long) 0L,
				builder().addFilter(TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_stringValue,
						FilterOperator.EQUAL, "related0").buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 1L,
				builder().addFilter(TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_stringValue,
						FilterOperator.EQUAL, "related1").buildExecutor().getUniqueResult(false).getId());
		assertEquals((Long) 2L,
				builder().addFilter(TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_stringValue,
						FilterOperator.EQUAL, "related2").buildExecutor().getUniqueResult(false).getId());
	}

	@Test
	public void testPrimitiveOrder() {
		assertEquals(5,
				builder().addOrderBy(Direction.DESC, TestEntity.ATTR_id).buildExecutor().getResultList().size());
		assertEquals((Long) 0L,
				builder().addOrderBy(Direction.ASC, TestEntity.ATTR_id).buildExecutor().getResultList().get(0).getId());
		assertEquals((Long) 4L, builder().addOrderBy(Direction.DESC, TestEntity.ATTR_id).buildExecutor().getResultList()
				.get(0).getId());

		assertEquals(5, builder().addOrderBy(Direction.DESC, TestEntity.ATTR_stringValue).buildExecutor()
				.getResultList().size());
		assertEquals((Long) 0L, builder().addOrderBy(Direction.ASC, TestEntity.ATTR_stringValue).buildExecutor()
				.getResultList().get(0).getId());
		assertEquals((Long) 4L, builder().addOrderBy(Direction.DESC, TestEntity.ATTR_stringValue).buildExecutor()
				.getResultList().get(0).getId());
	}

	@Test
	public void testEmbeddedOrder() {
		assertEquals(5, builder().addOrderBy(Direction.DESC, TestEntity.ATTR_embValue_intValue).buildExecutor()
				.getResultList().size());
		assertEquals((Long) 0L, builder().addOrderBy(Direction.ASC, TestEntity.ATTR_embValue_intValue).buildExecutor()
				.getResultList().get(0).getId());
		assertEquals((Long) 4L, builder().addOrderBy(Direction.DESC, TestEntity.ATTR_embValue_intValue).buildExecutor()
				.getResultList().get(0).getId());
	}

	@Test
	public void testOneRelatedOrder() {
		assertEquals(5, builder().setDefaultJoinType(JoinType.LEFT)
				.addOrderBy(Direction.DESC, TestEntity.ATTR_oneRelatedValue).buildExecutor().getResultList().size());
		assertEquals((Long) 0L, builder().addOrderBy(Direction.ASC, TestEntity.ATTR_oneRelatedValue).buildExecutor()
				.getResultList().get(0).getId());
		assertEquals((Long) 3L, builder().addOrderBy(Direction.DESC, TestEntity.ATTR_oneRelatedValue).buildExecutor()
				.getResultList().get(0).getId());
	}

	@Test
	public void testMapOrder() {
		assertEquals(5, builder().setDefaultJoinType(JoinType.LEFT)
				.addOrderBy(Direction.DESC, TestEntity.ATTR_mapValue + ".a").buildExecutor().getResultList().size());

		// one could argue about that...
		assertEquals(4, builder().setDefaultJoinType(JoinType.INNER)
				.addOrderBy(Direction.DESC, TestEntity.ATTR_mapValue + ".a").buildExecutor().getResultList().size());

		assertEquals((Long) 0L, builder().addOrderBy(Direction.ASC, TestEntity.ATTR_mapValue + ".a").buildExecutor()
				.getResultList().get(0).getId());
		assertEquals((Long) 3L, builder().addOrderBy(Direction.DESC, TestEntity.ATTR_mapValue + ".a").buildExecutor()
				.getResultList().get(0).getId());
	}

	@Test
	public void testTotalOrderNoSorting() {
		testPaging(false);

		QueryExecutor<TestEntity> exec = builder().buildExecutor();
		for (int i = 0; i < 5; i++) {
			exec.setWindow(i, 1);
			TestEntity entity = exec.getUniqueResult(false);
			assertEquals(i, entity.getId().intValue());
		}
	}

	@Test
	public void testTotalOrderNoTotalSorting() {
		QueryExecutor<TestEntity> exec = builder()
				.addOrderBy(Direction.ASC, TestEntity.ATTR_embValue_nestedValue_boolValue).buildExecutor();
		for (int i = 0; i < 5; i++) {
			exec.setWindow(i, 1);
			TestEntity entity = exec.getUniqueResult(false);
			if (i == 4) {
				assertTrue(entity.getEmbValue().getNestedValue().getEmbBoolValue());
				assertEquals(0, entity.getId().intValue());
			} else {
				assertFalse(entity.getEmbValue().getNestedValue().getEmbBoolValue());
				assertEquals(1 + i, entity.getId().intValue());
			}
		}
	}

	@Test
	public void testPaging() {
		testPaging(true);
	}

	private void testPaging(boolean applySorting) {
		QueryBuilder<TestEntity> builder = builder();
		if (applySorting) {
			builder.addOrderBy(Direction.DESC, TestEntity.ATTR_id);
		}
		QueryExecutor<TestEntity> exec = builder().buildExecutor();

		assertEquals(5, exec.getResultList().size());

		// repeat
		assertEquals(5, exec.getResultList().size());

		// apply paging
		assertEquals(4, exec.setWindow(1, -1).getResultList().size());
		assertEquals(3, exec.setWindow(2, -1).getResultList().size());
		assertEquals(2, exec.setWindow(3, -1).getResultList().size());
		assertEquals(1, exec.setWindow(4, -1).getResultList().size());
		assertEquals(0, exec.setWindow(5, -1).getResultList().size());
		assertEquals(0, exec.setWindow(6, -1).getResultList().size());

		assertEquals(2, exec.setWindow(1, 2).getResultList().size());
		assertEquals(2, exec.setWindow(2, 2).getResultList().size());
		assertEquals(2, exec.setWindow(3, 2).getResultList().size());
		assertEquals(1, exec.setWindow(4, 2).getResultList().size());
		assertEquals(0, exec.setWindow(5, 2).getResultList().size());
	}

	@Test
	public void testFilterNull() {
		assertEquals(5, builder().buildExecutor().getResultList().size());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.NOT_EQUAL, null)
				.buildExecutor().getResultList().size());

		// NOTE one could argue about the left join...
		assertEquals(1,
				builder().setDefaultJoinType(JoinType.LEFT)
						.addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.EQUAL, null).buildExecutor()
						.getResultList().size());
	}

	@Test
	public void testWithGraphControlWithoutJoin() {
		QueryExecutor<TestEntity> exec = builder().buildExecutor().fetch(TestEntity.ATTR_oneRelatedValue);
		for (TestEntity test : exec.getResultList()) {
			assertTrue(Hibernate.isInitialized(test));

			RelatedEntity relatedValue = test.getOneRelatedValue();
			if (relatedValue != null) {
				assertTrue(Hibernate.isInitialized(relatedValue));
			}
		}
	}

	@Test
	public void testWithGraphControlWithJoin() {
		QueryExecutor<TestEntity> exec = builder()
				.addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.NOT_EQUAL, null).buildExecutor()
				.fetch(TestEntity.ATTR_oneRelatedValue);
		for (TestEntity test : exec.getResultList()) {
			assertTrue(Hibernate.isInitialized(test));
			assertTrue(Hibernate.isInitialized(test.getOneRelatedValue()));
		}
	}

	@Test
	public void testWithoutGraphControl() {
		QueryExecutor<TestEntity> exec = builder()
				.addFilter(TestEntity.ATTR_oneRelatedValue, FilterOperator.NOT_EQUAL, null).buildExecutor();
		for (TestEntity test : exec.getResultList()) {
			RelatedEntity relatedValue = test.getOneRelatedValue();
			assertTrue(Hibernate.isInitialized(test));
			assertFalse(Hibernate.isInitialized(relatedValue));
		}
	}

	// NOTE enable with Java 8
	// @Test
	// public void testDateTime() {
	// assertEquals(5, builder().addFilter(TestEntity.ATTR_localDateValue,
	// FilterOperator.LESS_EQUAL, LocalDate.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0, builder().addFilter(TestEntity.ATTR_localDateValue,
	// FilterOperator.GREATER, LocalDate.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5, builder().addFilter(TestEntity.ATTR_localTimeValue,
	// FilterOperator.LESS_EQUAL, LocalTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0, builder().addFilter(TestEntity.ATTR_localTimeValue,
	// FilterOperator.GREATER, LocalTime.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5,
	// builder().addFilter(TestEntity.ATTR_localDateTimeValue,
	// FilterOperator.LESS_EQUAL, LocalDateTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0,
	// builder().addFilter(TestEntity.ATTR_localDateTimeValue,
	// FilterOperator.GREATER, LocalDateTime.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5,
	// builder()
	// .addFilter(TestEntity.ATTR_offsetDateTimeValue,
	// FilterOperator.LESS_EQUAL, OffsetDateTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0,
	// builder().addFilter(TestEntity.ATTR_offsetDateTimeValue,
	// FilterOperator.GREATER, OffsetDateTime.now())
	// .buildExecutor().getResultList().size());
	//
	// assertEquals(5,
	// builder().addFilter(TestEntity.ATTR_offsetTimeValue,
	// FilterOperator.LESS_EQUAL, OffsetTime.now())
	// .buildExecutor().getResultList().size());
	// assertEquals(0, builder().addFilter(TestEntity.ATTR_offsetTimeValue,
	// FilterOperator.GREATER, OffsetTime.now())
	// .buildExecutor().getResultList().size());
	// }

	@Test
	public void testJoinType() {
		// note one entity has no relation
		assertEquals(4,
				builder().setDefaultJoinType(JoinType.INNER)
						.addOrderBy(Direction.ASC, TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_id)
						.buildExecutor().getResultList().size());
		assertEquals(5,
				builder().setDefaultJoinType(JoinType.LEFT)
						.addOrderBy(Direction.ASC, TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_id)
						.buildExecutor().getResultList().size());
		assertEquals(4,
				builder().setJoinType(JoinType.INNER, TestEntity.ATTR_oneRelatedValue)
						.addOrderBy(Direction.ASC, TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_id)
						.buildExecutor().getResultList().size());
		assertEquals(5,
				builder().setJoinType(JoinType.LEFT, TestEntity.ATTR_oneRelatedValue)
						.addOrderBy(Direction.ASC, TestEntity.ATTR_oneRelatedValue + "." + RelatedEntity.ATTR_id)
						.buildExecutor().getResultList().size());
	}

	@Test
	public void testAnyType() {
		assertEquals(0, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQUAL, "first")
				.buildExecutor().getUniqueResult(false).getId().intValue());
		assertEquals(1, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQUAL, 1).buildExecutor()
				.getUniqueResult(false).getId().intValue());
		assertEquals(2, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQUAL, 2).buildExecutor()
				.getUniqueResult(false).getId().intValue());
		assertEquals(3, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQUAL, 3).buildExecutor()
				.getUniqueResult(false).getId().intValue());
		assertEquals(4, builder().addFilter(TestEntity.ATTR_embValue_anyValue, FilterOperator.EQUAL, 4).buildExecutor()
				.getUniqueResult(false).getId().intValue());

		List<TestEntity> list = builder().addOrderBy(Direction.DESC, TestEntity.ATTR_embValue_anyValue).buildExecutor()
				.getResultList();
		assertEquals(5, list.size());
		assertEquals("first", list.get(0).getEmbValue().getAnyValue().getValue());
		assertEquals(4, list.get(1).getEmbValue().getAnyValue().getValue());
		assertEquals(3, list.get(2).getEmbValue().getAnyValue().getValue());
		assertEquals(2, list.get(3).getEmbValue().getAnyValue().getValue());
		assertEquals(1, list.get(4).getEmbValue().getAnyValue().getValue());
	}
}
