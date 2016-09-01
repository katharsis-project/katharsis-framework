package io.katharsis.queryspec;

import org.junit.Assert;
import org.junit.Test;

public class FilterOperatorRegistryTest {

	@Test
	public void test() {
		FilterOperatorRegistry registry = new FilterOperatorRegistry();
		registry.register(FilterOperator.EQ);
		registry.register(FilterOperator.NEQ);
		registry.setDefaultOperator(FilterOperator.EQ);
		Assert.assertEquals(FilterOperator.EQ, registry.getDefaultOperator());

		Assert.assertEquals(FilterOperator.EQ, FilterOperator.EQ);
		Assert.assertNotEquals(FilterOperator.NEQ, FilterOperator.EQ);
		Assert.assertSame(FilterOperator.EQ, registry.get("EQ"));
	}
}
