package io.katharsis.queryspec;

import io.katharsis.utils.CompareUtils;
import io.katharsis.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paging implementation that uses page number and page size
 * as the paging inputs.
 * <p>
 * Looking for offset+limit based paging? See {@link PageBasedPagingSpec}.
 */
public class PageBasedPagingSpec implements PagingSpec {
    private final long pageNumber;
    private final long pageSize;

    private String pageNumberName = "number";
    private String pageSizeName = "size";

    /**
     * Create a page based paging spec with the specified page number and page size
     *
     * @param pageNumber
     * @param pageSize
     */
    public PageBasedPagingSpec(long pageNumber, long pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    /**
     * Create a page based paging spec with the specified page number and page size
     *
     * @param pageNumber
     * @param pageSize
     * @param pageNumberName the serialized name of the page number, defaults to "number"
     * @param pageSizeName   the serialized name of the page size, defaults to "size"
     */
    public PageBasedPagingSpec(long pageNumber, long pageSize, String pageNumberName, String pageSizeName) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.pageNumberName = pageNumberName;
        this.pageSizeName = pageSizeName;
    }

    @Override
    public <T> List<T> applyPaging(List<T> results) {
        long calculatedOffset = getOffset();
        int actualOffset = (int) Math.min(calculatedOffset, Integer.MAX_VALUE);
        int actualLimit = (int) Math.min(getLimit(), Integer.MAX_VALUE);

        return OffsetBasedPagingSpec.applyPaging(actualLimit, actualOffset, results);
    }

    @Override
    /**
     * Returns the offset according to the underlying page and page size.
     *
     * @return the calculated offset
     */
    public long getOffset() {
        return pageNumber * pageSize;
    }

    /**
     * The page represented
     *
     * @return
     */
    public long getPageNumber() {
        return pageNumber;
    }


    /**
     * The number of items to be returned
     *
     * @return number of items in the page
     */
    @Override
    public long getLimit() {
        return pageSize;
    }

    /**
     * A map containing:
     * pageNumberName : pageNumber
     * pageSizeName   : pageSize
     *
     * @return
     */
    @Override
    public Map<String, String> getSerializationNameValues() {
        Map<String, String> map = new HashMap<>();
        map.put(pageNumberName, Long.toString(getPageNumber()));
        map.put(pageSizeName, Long.toString(getLimit()));
        return map;
    }

    /**
     * The serialized name of the page number, defaults to "number"
     *
     * @return
     */
    public String getPageNumberName() {
        return pageNumberName;
    }

    public void setPageNumberName(String pageNumberName) {
        if (StringUtils.isBlank(pageNumberName))
            throw new IllegalArgumentException("Page number name should not be null or empty.");
        this.pageNumberName = pageNumberName;
    }

    /**
     * The serialized name of the page number, defaults to "size"
     *
     * @return
     */
    public String getPageSizeName() {
        return pageSizeName;
    }

    public void setPageSizeName(String pageSizeName) {
        if (StringUtils.isBlank(pageSizeName))
            throw new IllegalArgumentException("Page size name should not be null or empty.");
        this.pageSizeName = pageSizeName;
    }

    @Override
    public PagingSpec duplicate() {
        return new PageBasedPagingSpec(getPageNumber(), getLimit(), pageNumberName, pageSizeName);
    }

    @Override
    public PagingSpec first() {
        return new PageBasedPagingSpec(0, getLimit(), pageNumberName, pageSizeName);
    }

    @Override
    public PagingSpec next(long totalCount, long totalPages) {
        return new PageBasedPagingSpec(getPageNumber() + 1, getLimit(), pageNumberName, pageSizeName);
    }

    @Override
    public PagingSpec prev(long totalCount, long totalPages) {
        return getPageNumber() == 0 ? this : new PageBasedPagingSpec(getPageNumber() - 1, getLimit());
    }

    @Override
    public PagingSpec last(long totalCount, long totalPages) {
        return new PageBasedPagingSpec(totalPages - 1, getLimit(), pageNumberName, pageSizeName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        PageBasedPagingSpec that = (PageBasedPagingSpec) o;

        return CompareUtils.isEquals(pageNumber, that.pageNumber)
                && CompareUtils.isEquals(pageSize, that.pageSize)
                && CompareUtils.isEquals(pageSizeName, that.pageSizeName)
                && CompareUtils.isEquals(pageNumberName, that.pageNumberName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(pageNumber).hashCode();
        result = prime * result + Long.valueOf(pageSize).hashCode();
        result = prime * result + pageSizeName.hashCode();
        result = prime * result + pageNumberName.hashCode();
        return result;
    }


}
