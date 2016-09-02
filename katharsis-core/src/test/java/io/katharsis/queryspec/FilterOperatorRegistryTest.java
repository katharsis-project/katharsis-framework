package io.katharsis.queryspec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilterOperatorRegistryTest {

	private FilterOperatorRegistry registry;

	@Before
	public void setup() {
		registry = new FilterOperatorRegistry();
		registry.register(FilterOperator.EQ);
		registry.register(FilterOperator.NEQ);
		registry.setDefaultOperator(FilterOperator.EQ);
	}

	@Test
	public void test() {
		Assert.assertEquals(FilterOperator.EQ, registry.getDefaultOperator());
		Assert.assertEquals(FilterOperator.EQ, FilterOperator.EQ);
		Assert.assertNotEquals(FilterOperator.NEQ, FilterOperator.EQ);
		Assert.assertSame(FilterOperator.EQ, registry.get("EQ"));
		Assert.assertNotEquals(FilterOperator.NEQ, null);
		Assert.assertNotEquals(FilterOperator.NEQ, "someDifferentObjectType");
		Assert.assertNotEquals(FilterOperator.NEQ, new FilterOperator("NEQ") {

			@Override
			public boolean matches(Object value1, Object value2) {
				return false;
			}
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownOperatorThrowsException() {
		registry.get("unknown");
	}

	@Test(expected = IllegalStateException.class)
	public void testNoDefaultOperatorThrowsException() {
		registry.setDefaultOperator(null);
		registry.getDefaultOperator();
	}
}
