package io.katharsis.queryspec.internal;

import io.katharsis.queryspec.PageBasedPagingSpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PageQuerySpecAdapterTest {
    PageBasedPagingSpec firstPage;
    PageBasedPagingSpec middlePage;
    PageBasedPagingSpec lastPage;
    PageQuerySpecAdapter firstPageAdapter;
    PageQuerySpecAdapter middlePageAdapter;
    PageQuerySpecAdapter lastPageAdapter;

    @Before
    public void setup() {
        firstPage = new PageBasedPagingSpec(0, 10);
        middlePage = new PageBasedPagingSpec(5, 10);
        lastPage = new PageBasedPagingSpec(10, 10);
        firstPageAdapter = new PageQuerySpecAdapter(firstPage, 100, 10);
        middlePageAdapter = new PageQuerySpecAdapter(middlePage, 100, 10);
        lastPageAdapter = new PageQuerySpecAdapter(lastPage, 100, 10);
    }

    @Test
    public void testDelegation() throws Exception {
        Assert.assertEquals(firstPage.getLimit(), firstPageAdapter.getLimit());
        Assert.assertEquals(firstPage.getOffset(), firstPageAdapter.getOffset());
        Assert.assertEquals(firstPage.next(firstPageAdapter.getTotalCount(), firstPageAdapter.getTotalPages()),
                ((PageQuerySpecAdapter) firstPageAdapter.next()).getPagingSpec());
        Assert.assertEquals(firstPage.prev(firstPageAdapter.getTotalCount(), firstPageAdapter.getTotalPages()),
                ((PageQuerySpecAdapter) firstPageAdapter.prev()).getPagingSpec());
        Assert.assertEquals(firstPage.last(firstPageAdapter.getTotalCount(), firstPageAdapter.getTotalPages()),
                ((PageQuerySpecAdapter) firstPageAdapter.last()).getPagingSpec());
        Assert.assertEquals(firstPage.first(), ((PageQuerySpecAdapter) firstPageAdapter.first()).getPagingSpec());
    }

    @Test
    public void hasNext() throws Exception {
        Assert.assertTrue(firstPageAdapter.hasNext());
        Assert.assertTrue(middlePageAdapter.hasNext());
        Assert.assertFalse(lastPageAdapter.hasNext());
    }

    @Test
    public void hasPrev() throws Exception {
        Assert.assertFalse(firstPageAdapter.hasPrev());
        Assert.assertTrue(middlePageAdapter.hasPrev());
        Assert.assertTrue(lastPageAdapter.hasPrev());
    }

    @Test
    public void testDuplicateEqualsAndHashCode() throws Exception {

        PageQuerySpecAdapter duplicate = (PageQuerySpecAdapter) firstPageAdapter.duplicate();
        Assert.assertEquals(firstPageAdapter.getLimit(), duplicate.getLimit());
        Assert.assertEquals(firstPageAdapter.getOffset(), duplicate.getOffset());
        Assert.assertEquals(firstPageAdapter.getTotalPages(), duplicate.getTotalPages());
        Assert.assertEquals(firstPageAdapter.getTotalCount(), duplicate.getTotalCount());
        Assert.assertEquals(firstPageAdapter.getPagingSpec(), duplicate.getPagingSpec());
        Assert.assertEquals(firstPageAdapter, duplicate);

        Assert.assertNotEquals(firstPageAdapter, "someOtherType");
    }

}