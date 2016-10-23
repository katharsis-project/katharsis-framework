package io.katharsis.queryspec;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class OffsetBasedPagingSpecTest {
    @Test
    public void applyPaging() throws Exception {
        List<String> list = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        OffsetBasedPagingSpec spec = new OffsetBasedPagingSpec(0, 1);

        List<String> result = spec.applyPaging(list);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("1", result.get(0));

        spec = new OffsetBasedPagingSpec(2, 4);
        result = spec.applyPaging(list);

        Assert.assertEquals(4, result.size());
        Assert.assertEquals(list.subList(2, 6), result);
    }

    @Test
    public void getSerializationNameValues() throws Exception {
        OffsetBasedPagingSpec spec = new OffsetBasedPagingSpec(5, 10, "foo", "bar");

        Map<String, String> serializationNameValues = spec.getSerializationNameValues();
        Assert.assertEquals("5", serializationNameValues.get("foo"));
        Assert.assertEquals("10", serializationNameValues.get("bar"));
    }

    @Test
    public void testDuplicateEqualsAndHashCode() throws Exception {
        OffsetBasedPagingSpec spec = new OffsetBasedPagingSpec(5, 10, "foo", "bar");
        OffsetBasedPagingSpec duplicate = (OffsetBasedPagingSpec) spec.duplicate();

        Assert.assertEquals(spec.getLimit(), duplicate.getLimit());
        Assert.assertEquals(spec.getOffset(), duplicate.getOffset());
        Assert.assertEquals(spec.getOffsetName(), duplicate.getOffsetName());
        Assert.assertEquals(spec.getLimitName(), duplicate.getLimitName());

        Assert.assertTrue(spec.equals(duplicate));
        Assert.assertEquals(spec.hashCode(), duplicate.hashCode());

        Assert.assertNotEquals(spec, "someOtherType");
    }

    @Test
    public void first() throws Exception {
        OffsetBasedPagingSpec spec = new OffsetBasedPagingSpec(5, 10);
        PagingSpec first = spec.first();
        Assert.assertEquals(spec.getLimit(), first.getLimit());
        Assert.assertEquals(0L, first.getOffset());

        OffsetBasedPagingSpec firstClone = new OffsetBasedPagingSpec(0, 10);
        Assert.assertTrue(first.equals(firstClone));
    }

    @Test
    public void next() throws Exception {
        OffsetBasedPagingSpec spec = new OffsetBasedPagingSpec(0, 10);
        PagingSpec next = spec.next(30, 3);
        Assert.assertEquals(spec.getLimit(), next.getLimit());
        Assert.assertEquals(10, next.getOffset());
    }

    @Test
    public void prev() throws Exception {
        // when prev() called on the first page, must result in the same page
        OffsetBasedPagingSpec spec = new OffsetBasedPagingSpec(0, 10);
        PagingSpec prev = spec.prev(30, 3);
        Assert.assertEquals(spec, prev);

        spec = new OffsetBasedPagingSpec(10, 5);
        prev = spec.prev(5 * 4, 4);
        Assert.assertEquals(spec.getLimit(), prev.getLimit());
        Assert.assertEquals(5, prev.getOffset());
    }

    @Test
    public void last() throws Exception {
        OffsetBasedPagingSpec spec = new OffsetBasedPagingSpec(0, 10);
        PagingSpec last = spec.last(30, 3);
        Assert.assertEquals(spec.getLimit(), last.getLimit());
        Assert.assertEquals(20, last.getOffset());
    }

}