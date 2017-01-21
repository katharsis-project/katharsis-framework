package io.katharsis.core.internal.dispatcher;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.utils.Nullable;

public class ResponseEqualsContractTest {

	@Test
	public void testHashCodeEquals() {
		Document r1 = new Document();
		Document r2 = new Document();
		r2.setData(Nullable.of((Object) new Resource()));
		Response c1 = new Response(r1, 201);
		Response c1copy = new Response(r1, 201);
		Response c2 = new Response(r2, 202);
		Response c3 = new Response(r1, 202);

		Assert.assertEquals(c1.hashCode(), c1copy.hashCode());
		Assert.assertTrue(c1.equals(c1));
		Assert.assertTrue(c1.equals(c1copy));
		Assert.assertFalse(c1.equals(c2));
		Assert.assertFalse(c1.equals(c3));
		Assert.assertFalse(c2.equals(c3));
		Assert.assertFalse(c2.equals("otherType"));
	}
}
