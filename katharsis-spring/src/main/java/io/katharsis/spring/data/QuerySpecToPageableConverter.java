package io.katharsis.spring.data;

import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to convert a Katharsis QuerySpec object to a Spring Data Pageable implementation. It will
 * convert the internal Katharsis SortSpecs to a Spring Data Sort objects. It will also handle changing from a
 * offset/limit based paging scheme that JSONAPI and Katharsis uses to the page number/page size scheme spring data
 * uses. It accomplishes this with the OffsetPageable class, which is an implementation of Pageable.
 *
 */
public class QuerySpecToPageableConverter extends BaseConverter {

    /**
     * Converts a QuerySpec to a Pageable implementation. It will
     * convert the internal Katharsis SortSpecs to a Spring Data Sort objects. It will also handle changing from a
     * offset/limit based paging scheme that JSONAPI and Katharsis uses to the page number/page size scheme spring data
     * uses. It accomplishes this with the OffsetPageable class, which is an implementation of Pageable.
     *
     * @param querySpec The QuerySpec object to convert, cannot be null
     * @param defaultPageLimit If limit is null, use this value
     * @return An OffsetPageable object
     */
    public OffsetPageable convert(QuerySpec querySpec, Long defaultPageLimit) {
        OffsetPageable result;

        Sort sort = convertSort(querySpec);

        Long limit = querySpec.getLimit();
        long offset = querySpec.getOffset();

        if (limit == null) {
            limit = defaultPageLimit;
        }

        if (sort == null) {
            result = new OffsetPageable(limit, offset);
        } else {
            result = new OffsetPageable(limit, offset, sort);
        }

        return result;
    }

    /**
     * Converts a Katharsis QuerySpec's SortSpecs into Spring Data Sort objects.
     *
     * @param querySpec The QuerySpec whose SortSpecs we need to convert
     * @return The converted Sort object or null
     */
    public Sort convertSort(QuerySpec querySpec) {
        Sort result = null;

        List<Sort.Order> sortOrders = new ArrayList<>();
        List<SortSpec> sortSpec = querySpec.getSort();


        for (SortSpec spec : sortSpec) {
            Sort.Direction dir = convertDirection(spec.getDirection());
            List<String> attributePath = spec.getAttributePath();

            // Property paths must be in dot notation for Spring's Sort.Order
            String propertyPath = convertAttributePathToString(attributePath);
            sortOrders.add(new Sort.Order(dir, propertyPath));
        }

        if (sortOrders.size() > 0) {
            result = new Sort(sortOrders);
        }

        return result;
    }

    /**
     * Converts a Katharsis Direction into a Spring Data Sort.Direction object
     *
     * @param specDirection The Direction to convert
     * @return The Sort.Direction or null if specDirection is null
     */
    public Sort.Direction convertDirection(Direction specDirection) {
        Sort.Direction result = null;

        if (specDirection != null) {
            result = Sort.Direction.valueOf(specDirection.name());
        }

        return result;
    }

}
