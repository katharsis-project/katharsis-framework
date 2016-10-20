package io.katharsis.utils;

import org.junit.Test;

public class PreconditionUtilTest {

	@Test
	public void testSatisfied() {
		PreconditionUtil.assertEquals(null, 1, 1);
		PreconditionUtil.assertTrue(null, true);
		PreconditionUtil.assertFalse(null, false);
		PreconditionUtil.assertNotNull(null, "test");
		PreconditionUtil.assertNull(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testEqualsNotSatisfied() {
		PreconditionUtil.assertEquals(null, 1, 2);
	}

	@Test(expected = IllegalStateException.class)
	public void testTrueNotSatisfied() {
		PreconditionUtil.assertTrue(null, false);
	}

	@Test(expected = IllegalStateException.class)
	public void testFalseNotSatisfied() {
		PreconditionUtil.assertFalse(null, true);
	}

	@Test(expected = IllegalStateException.class)
	public void testNotNullNotSatisfied() {
		PreconditionUtil.assertNotNull(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testNullNotSatisfied() {
		PreconditionUtil.assertNull(null, "not null");
	}
}
