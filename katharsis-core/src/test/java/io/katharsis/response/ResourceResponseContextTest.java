package io.katharsis.response;

import org.junit.Assert;
import org.junit.Test;

public class ResourceResponseContextTest {

	@Test
	public void testHashCodeEquals() {
		JsonApiResponse r1 = new JsonApiResponse();
		JsonApiResponse r2 = new JsonApiResponse();
		ResourceResponseContext c1 = new ResourceResponseContext(r1, 201);
		ResourceResponseContext c1copy = new ResourceResponseContext(r1, 201);
		ResourceResponseContext c2 = new ResourceResponseContext(r2, 202);
		ResourceResponseContext c3 = new ResourceResponseContext(r1, 202);

		Assert.assertEquals(c1.hashCode(), c1copy.hashCode());
		Assert.assertTrue(c1.equals(c1));
		Assert.assertTrue(c1.equals(c1copy));
		Assert.assertFalse(c1.equals(c2));
		Assert.assertFalse(c1.equals(c3));
		Assert.assertFalse(c2.equals(c3));
		Assert.assertFalse(c2.equals("otherType"));
	}
}
