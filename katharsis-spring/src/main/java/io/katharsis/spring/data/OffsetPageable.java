package io.katharsis.spring.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * The OffsetPageable class is an implementation of the Spring Data's Pageable interface. Instead of the default
 * behavior of using page size and page number, it will use a index offset and limit to retrieve the page information.
 * It will recalculate the pages if the offset ends up being negative when using the previous page functionality.
 *
 */
class OffsetPageable implements Pageable {
    private Long limit;
    private long offset;
    private Sort sort;

    OffsetPageable(Long limit, long offset) {
        this.limit = limit;
        this.offset = offset;
        this.sort = null;
    }

    OffsetPageable(Long limit, long offset, Sort sort) {
        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
    }

    @Override
    public int getPageNumber() {
        if (limit == null) {
            return 0;
        } else {
            return (new Double(Math.ceil(offset/limit))).intValue();
        }
    }

    @Override
    public int getPageSize() {
        if (limit == null) {
            return 0;
        } else {
            return limit.intValue();
        }
    }

    @Override
    public int getOffset() {
        return (new Long(offset)).intValue();
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        if (limit == null) {
            return null;
        }

        long nextOffset = offset + limit;

        if (sort == null) {
            return new OffsetPageable(limit, nextOffset);
        } else {
            return new OffsetPageable(limit, nextOffset, sort);
        }
    }

    @Override
    public Pageable previousOrFirst() {
        if (limit == null) {
            return null;
        }

        long prevOffset = offset - limit;

        if (prevOffset < 0) {
            prevOffset = 0;
        }

        if (sort == null) {
            return new OffsetPageable(limit, prevOffset);
        } else {
            return new OffsetPageable(limit, prevOffset, sort);
        }
    }

    @Override
    public Pageable first() {
        if (sort == null) {
            return new OffsetPageable(limit, 0);
        } else {
            return new OffsetPageable(limit, 0, sort);
        }
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
