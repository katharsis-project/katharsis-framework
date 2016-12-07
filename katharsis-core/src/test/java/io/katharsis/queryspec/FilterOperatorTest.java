package io.katharsis.queryspec;

import org.junit.Assert;
import org.junit.Test;

public class FilterOperatorTest {

	@Test(expected = UnsupportedOperationException.class)
	public void andMatchNotSupported() {
		FilterOperator.AND.matches(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void notMatchNotSupported() {
		FilterOperator.NOT.matches(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void orMatchNotSupported() {
		FilterOperator.OR.matches(null, null);
	}

	@Test
	public void testEquals() {
		Assert.assertEquals(FilterOperator.AND, FilterOperator.AND);
		Assert.assertNotEquals(FilterOperator.AND, "notAnOperator");
		Assert.assertEquals(FilterOperator.OR, FilterOperator.OR);
		Assert.assertEquals(FilterOperator.OR, new FilterOperator("OR") {

			@Override
			public boolean matches(Object value1, Object value2) {
				return false;
			}
		});
		Assert.assertNotEquals(FilterOperator.AND, FilterOperator.OR);
	}
}
