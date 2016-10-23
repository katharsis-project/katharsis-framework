package io.katharsis.queryspec.internal;

import io.katharsis.queryspec.PagingSpec;

public class PageQuerySpecAdapter implements PageQueryAdapter {
    private final PagingSpec pagingSpec;
    private long totalCount;
    private long totalPages;

    public PageQuerySpecAdapter(PagingSpec pagingSpec) {
        this.pagingSpec = pagingSpec;
    }


    public PageQuerySpecAdapter(PagingSpec pagingSpec, long totalCount, long totalPages) {
        this.pagingSpec = pagingSpec;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
    }

    public PagingSpec getPagingSpec() {
        return pagingSpec;
    }

    @Override
    public long getTotalPages() {
        return totalPages;
    }

    @Override
    public long getTotalCount() {
        return totalCount;
    }

    @Override
    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;

    }

    @Override
    public long getOffset() {
        return pagingSpec.getOffset();
    }

    @Override
    public long getLimit() {
        return pagingSpec.getLimit();
    }

    @Override
    public PageQueryAdapter first() {
        return new PageQuerySpecAdapter(pagingSpec.first(), totalCount, totalPages);
    }

    @Override
    public boolean hasNext() {
        long currentPage = getOffset() / getLimit();
        return currentPage + 1 < getTotalPages();
    }

    @Override
    public PageQueryAdapter next() {
        return new PageQuerySpecAdapter(pagingSpec.next(totalCount, totalPages), totalCount, totalPages);
    }

    @Override
    public boolean hasPrev() {
        long currentPage = getOffset() / getLimit();
        return currentPage > 0;
    }

    @Override
    public PageQueryAdapter prev() {
        return new PageQuerySpecAdapter(pagingSpec.prev(totalCount, totalPages), totalCount, totalPages);
    }

    @Override
    public PageQueryAdapter last() {
        return new PageQuerySpecAdapter(pagingSpec.last(totalCount, totalPages), totalCount, totalPages);
    }

    @Override
    public PageQueryAdapter duplicate() {
        return new PageQuerySpecAdapter(pagingSpec.duplicate(), totalCount, totalPages);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageQuerySpecAdapter that = (PageQuerySpecAdapter) o;

        if (totalCount != that.totalCount) return false;
        if (totalPages != that.totalPages) return false;
        return pagingSpec.equals(that.pagingSpec);

    }

    @Override
    public int hashCode() {
        int result = pagingSpec.hashCode();
        result = 31 * result + (int) (totalCount ^ (totalCount >>> 32));
        result = 31 * result + (int) (totalPages ^ (totalPages >>> 32));
        return result;
    }
}
