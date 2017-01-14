package io.katharsis.jpa.query;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.model.TestEntity;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;

@Transactional
public abstract class ComputedAttributeTestBase extends AbstractJpaTest {

	protected static final String ATTR_VIRTUAL_VALUE = "virtualValue";

	private JpaQuery<TestEntity> builder() {
		return queryFactory.query(TestEntity.class);
	}

	@Test
	public void testEqualsFilter() {

		assertEquals((Long) 1L, builder().addFilter(ATTR_VIRTUAL_VALUE, FilterOperator.EQ, "TEST1").buildExecutor().getUniqueResult(false).getId());

	}

	@Test
	public void testSelection() {
		JpaQuery<TestEntity> query = builder();
		query.addSelection(Arrays.asList(ATTR_VIRTUAL_VALUE));
		query.addSortBy(Arrays.asList(TestEntity.ATTR_stringValue), Direction.ASC);

		List<Tuple> resultList = query.buildExecutor().getResultTuples();
		Assert.assertEquals(5, resultList.size());
		for (int i = 0; i < resultList.size(); i++) {
			Tuple tuple = resultList.get(i);
			Assert.assertEquals("TEST" + i, tuple.get(ATTR_VIRTUAL_VALUE, String.class));
		}
	}

}
