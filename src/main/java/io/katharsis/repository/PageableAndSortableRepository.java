package io.katharsis.repository;

import io.katharsis.queryParams.SortingValues;

import java.io.Serializable;
import java.util.Map;

public interface PageableAndSortableRepository<T, ID extends Serializable>
        extends PageableRepository<T, ID>, SortableRepository<T, ID> {
    Iterable<T> findPage(int offset, int limit, Map<String, SortingValues> sortingValuesMap);
}
