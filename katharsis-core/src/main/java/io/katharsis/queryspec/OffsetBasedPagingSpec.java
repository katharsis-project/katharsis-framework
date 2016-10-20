package io.katharsis.queryspec;

import io.katharsis.utils.CompareUtils;
import io.katharsis.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Offset+Limit based paging implementation where offset and limit (here called limit) are the primary
 * paging inputs.
 * <p>
 * Looking for page number and size based paging? See {@link PageBasedPagingSpec}.
 */
public class OffsetBasedPagingSpec implements PagingSpec {
    private final long offset;
    private final long limit;


    private String offsetName = "offset";
    private String limitName = "limit";

    /**
     * Construct an offset based paging spec with default serialization names.
     *
     * @param offset
     * @param limit
     */
    public OffsetBasedPagingSpec(long offset, long limit) {
        this.offset = offset;
        this.limit = limit;
    }

    /**
     * Construct an offset based paging spec with customized serialization names.
     *
     * @param offset
     * @param limit
     * @param offsetName
     * @param limitName
     */
    public OffsetBasedPagingSpec(long offset, long limit, String offsetName, String limitName) {
        this.offset = offset;
        this.limit = limit;
        this.offsetName = offsetName;
        this.limitName = limitName;
    }

    @Override
    public <T> List<T> applyPaging(List<T> results) {
        int actualOffset = (int) Math.min(getOffset(), Integer.MAX_VALUE);
        int actualLimit = (int) Math.min(getLimit(), Integer.MAX_VALUE);

        return OffsetBasedPagingSpec.applyPaging(actualLimit, actualOffset, results);
    }

    public static <T> List<T> applyPaging(int limit, int offset, List<T> results) {
        limit = Math.min(results.size() - offset, limit);
        if (offset > 0 || limit < results.size()) {
            return results.subList(offset, offset + limit);
        }
        return results;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    /**
     * The serialized name of the offset, defaults to "offset"
     *
     * @return
     */
    public String getOffsetName() {
        return offsetName;
    }

    public void setOffsetName(String offsetName) {
        if (StringUtils.isBlank(offsetName))
            throw new IllegalArgumentException("Offset name should not be null or empty.");
        this.offsetName = offsetName;
    }


    /**
     * The serialized name of the limit, defaults to "limit"
     *
     * @return
     */
    public String getLimitName() {
        return limitName;
    }

    public void setLimitName(String limitName) {
        if (StringUtils.isBlank(limitName))
            throw new IllegalArgumentException("Limit name should not be null or empty.");
        this.limitName = limitName;
    }

    /**
     * A map containing:
     * offsetName : offset
     * limitName  : limit
     *
     * @return
     */
    @Override
    public Map<String, String> getSerializationNameValues() {
        Map<String, String> map = new HashMap<>();
        map.put(offsetName, Long.toString(getOffset()));
        map.put(limitName, Long.toString(getLimit()));
        return map;
    }

    @Override
    public PagingSpec duplicate() {
        return new OffsetBasedPagingSpec(offset, limit, offsetName, limitName);
    }

    @Override
    public PagingSpec first() {
        return new OffsetBasedPagingSpec(0, limit, offsetName, limitName);
    }

    @Override
    public PagingSpec next(long totalCount, long totalPages) {
        return new OffsetBasedPagingSpec(Math.min(totalCount, offset + limit), limit, offsetName, limitName);
    }

    @Override
    public PagingSpec prev(long totalCount, long totalPages) {
        return new OffsetBasedPagingSpec(Math.max(0, offset - limit), limit, offsetName, limitName);
    }

    @Override
    public PagingSpec last(long totalCount, long totalPages) {
        return new OffsetBasedPagingSpec((totalPages - 1) * limit, limit, offsetName, limitName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        OffsetBasedPagingSpec that = (OffsetBasedPagingSpec) o;

        return CompareUtils.isEquals(offset, that.offset)
                && CompareUtils.isEquals(limit, that.limit)
                && CompareUtils.isEquals(limitName, that.limitName)
                && CompareUtils.isEquals(offsetName, that.offsetName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(offset).hashCode();
        result = prime * result + Long.valueOf(limit).hashCode();
        result = prime * result + limitName.hashCode();
        result = prime * result + offsetName.hashCode();
        return result;
    }

}
