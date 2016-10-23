package io.katharsis.queryspec.internal;

public interface PageQueryAdapter {

    /**
     * The total number of pages for the query.
     *
     * @return
     */
    long getTotalPages();

    /**
     * The total number of items across all pages in the query.
     *
     * @return
     */
    long getTotalCount();

    void setTotalPages(long totalPages);

    void setTotalCount(long totalCount);

    /**
     * The current offset for this page
     *
     * @return
     */
    long getOffset();

    /**
     * The current limit for this page
     *
     * @return
     */
    long getLimit();

    /**
     * Get the PagingSpec for the first page
     *
     * @return
     */
    PageQueryAdapter first();

    /**
     * Returns whether there is a next page that can be accessed from the current page
     *
     * @return
     */
    boolean hasNext();

    /**
     * Get the PagingSpec for the next page
     *
     * @return
     */
    PageQueryAdapter next();

    /**
     * Returns whether there is a previous page that can be accessed from the current page
     *
     * @return
     */
    boolean hasPrev();

    /**
     * Get the PagingSpec for the previous page
     *
     * @return
     */
    PageQueryAdapter prev();

    /**
     * Get the PagingSpec for the last page
     *
     * @return
     */
    PageQueryAdapter last();

    /**
     * @return clone of this instance
     */
    PageQueryAdapter duplicate();
}
