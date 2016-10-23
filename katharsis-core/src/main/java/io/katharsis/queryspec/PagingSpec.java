package io.katharsis.queryspec;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * An abstract interface for paging implementations.
 * <p>
 * See one of the default implementations:
 * <p>
 * {@link PageBasedPagingSpec} or
 * {@link OffsetBasedPagingSpec}
 */
public interface PagingSpec extends Serializable {

    /**
     * Applies the paging parameters to the supplied list and returns the appropriate page.
     *
     * @param results
     * @param <T>
     * @return the sub list matching the paging spec
     */
    <T> List<T> applyPaging(List<T> results);

    /**
     * Returns the offset to be taken according to the underlying page and page size.
     *
     * @return the offset to be take
     */
    long getOffset();

    /**
     * Returns the number of items to be returned from the page.
     *
     * @return the number of items of the page
     */
    long getLimit();

    /**
     * A map of the paging parameters and value. It is used to serialize the paging spec to query parameters.
     * <p>
     * See the {@link PageBasedPagingSpec#getSerializationNameValues()} or
     * {@link OffsetBasedPagingSpec#getSerializationNameValues()} for examples.
     *
     * @return
     */
    Map<String, String> getSerializationNameValues();

    /**
     * Return a clone of this instance
     */
    PagingSpec duplicate();

    /**
     * Get the PagingSpec for the first page
     *
     * @return
     */
    PagingSpec first();

    /**
     * Get the PagingSpec for the next page, given a total count and pages
     *
     * @param totalCount
     * @param totalPages
     * @return
     */
    PagingSpec next(long totalCount, long totalPages);

    /**
     * Get the PagingSpec for the previous page, given a total count and pages
     *
     * @param totalCount
     * @param totalPages
     * @return
     */
    PagingSpec prev(long totalCount, long totalPages);

    /**
     * Get the PagingSpec for the last page, given a total count and pages
     *
     * @param totalCount
     * @param totalPages
     * @return
     */
    PagingSpec last(long totalCount, long totalPages);
}
