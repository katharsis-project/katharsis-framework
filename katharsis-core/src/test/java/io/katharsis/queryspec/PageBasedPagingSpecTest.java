package io.katharsis.queryspec;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PageBasedPagingSpecTest {
    @Test
    public void applyPaging() throws Exception {
        List<String> list = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        PageBasedPagingSpec spec = new PageBasedPagingSpec(0, 1);

        List<String> result = spec.applyPaging(list);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("1", result.get(0));

        spec = new PageBasedPagingSpec(2, 4);
        result = spec.applyPaging(list);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(list.subList(8, 10), result);
    }

    @Test
    public void getSerializationNameValues() throws Exception {
        PageBasedPagingSpec spec = new PageBasedPagingSpec(5, 10, "foo", "bar");

        Map<String, String> serializationNameValues = spec.getSerializationNameValues();
        Assert.assertEquals("5", serializationNameValues.get("foo"));
        Assert.assertEquals("10", serializationNameValues.get("bar"));
    }

    @Test
    public void testDuplicateEqualsAndHashCode() throws Exception {
        PageBasedPagingSpec spec = new PageBasedPagingSpec(5, 10, "foo", "bar");
        PageBasedPagingSpec duplicate = (PageBasedPagingSpec) spec.duplicate();

        Assert.assertEquals(spec.getLimit(), duplicate.getLimit());
        Assert.assertEquals(spec.getOffset(), duplicate.getOffset());
        Assert.assertEquals(spec.getPageNumber(), duplicate.getPageNumber());

        Assert.assertTrue(spec.equals(duplicate));
        Assert.assertEquals(spec.hashCode(), duplicate.hashCode());

        Assert.assertNotEquals(spec, "someOtherType");
    }

    @Test
    public void first() throws Exception {
        PageBasedPagingSpec spec = new PageBasedPagingSpec(5, 10);
        PageBasedPagingSpec first = (PageBasedPagingSpec) spec.first();
        Assert.assertEquals(spec.getLimit(), first.getLimit());
        Assert.assertEquals(0L, first.getOffset());
        Assert.assertEquals(0L, first.getPageNumber());

        PageBasedPagingSpec firstClone = new PageBasedPagingSpec(0, 10);
        Assert.assertTrue(first.equals(firstClone));
    }

    @Test
    public void next() throws Exception {
        PageBasedPagingSpec spec = new PageBasedPagingSpec(0, 10);
        PageBasedPagingSpec next = (PageBasedPagingSpec) spec.next(30, 3);
        Assert.assertEquals(spec.getLimit(), next.getLimit());
        Assert.assertEquals(10, next.getOffset());
        Assert.assertEquals(1, next.getPageNumber());
    }

    @Test
    public void prev() throws Exception {
        // when prev() called on the first page, must result in the same page
        PageBasedPagingSpec spec = new PageBasedPagingSpec(0, 10);
        PageBasedPagingSpec prev = (PageBasedPagingSpec) spec.prev(30, 3);
        Assert.assertEquals(spec, prev);

        spec = new PageBasedPagingSpec(10, 5);
        prev = (PageBasedPagingSpec) spec.prev(10 * 5, 10);
        Assert.assertEquals(spec.getLimit(), prev.getLimit());
        Assert.assertEquals(9, prev.getPageNumber());
    }

    @Test
    public void last() throws Exception {
        PageBasedPagingSpec spec = new PageBasedPagingSpec(0, 10);
        PageBasedPagingSpec last = (PageBasedPagingSpec) spec.last(30, 3);
        Assert.assertEquals(spec.getLimit(), last.getLimit());
        Assert.assertEquals(20, last.getOffset());
        Assert.assertEquals(2, last.getPageNumber());
    }

}