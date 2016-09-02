package io.katharsis.jpa.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.model.TestEntity;
import io.katharsis.queryspec.FilterOperator;

@Transactional
public abstract class VirtualAttributeTestBase extends AbstractJpaTest {

	protected static final String ATTR_VIRTUAL_VALUE = "virtualValue";

	private JpaQuery<TestEntity> builder() {
		return queryFactory.query(TestEntity.class);
	}

	@Test
	public void testEqualsFilter() {

		assertEquals((Long) 1L, builder().addFilter(ATTR_VIRTUAL_VALUE, FilterOperator.EQ, "TEST1").buildExecutor()
				.getUniqueResult(false).getId());

	}

}
