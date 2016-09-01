package io.katharsis.queryspec;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class SortSpecTest {

	@Test
	public void testBasic() {
		SortSpec spec = new SortSpec(Arrays.asList("name"), Direction.ASC);
		Assert.assertEquals(Direction.ASC, spec.getDirection());
		Assert.assertEquals(Arrays.asList("name"), spec.getAttributePath());
	}

	@Test
	public void testToString() {
		Assert.assertEquals("name ASC", new SortSpec(Arrays.asList("name"), Direction.ASC).toString());
		Assert.assertEquals("name1.name2 ASC", new SortSpec(Arrays.asList("name1", "name2"), Direction.ASC).toString());
		Assert.assertEquals("name DESC", new SortSpec(Arrays.asList("name"), Direction.DESC).toString());
	}

	@Test
	public void testEquals() {
		SortSpec spec1 = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec spec2 = new SortSpec(Arrays.asList("name1"), Direction.ASC);
		SortSpec spec3 = new SortSpec(Arrays.asList("name2"), Direction.ASC);
		SortSpec spec4 = new SortSpec(Arrays.asList("name1"), Direction.DESC);

		Assert.assertEquals(spec1, spec1);
		Assert.assertEquals(spec3, spec3);
		Assert.assertEquals(spec1, spec2);
		Assert.assertEquals(spec2, spec1);
		Assert.assertEquals(spec1.hashCode(), spec1.hashCode());
		Assert.assertEquals(spec3.hashCode(), spec3.hashCode());
		Assert.assertEquals(spec1.hashCode(), spec2.hashCode());
		Assert.assertNotEquals(spec2, spec3);
		Assert.assertNotEquals(spec3, spec2);
		Assert.assertNotEquals(spec1, spec4);
		Assert.assertNotEquals(spec3, spec4);
	}
}
