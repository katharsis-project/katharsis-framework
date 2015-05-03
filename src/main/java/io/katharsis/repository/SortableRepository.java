package io.katharsis.repository;

import io.katharsis.queryParams.SortingValues;

import java.io.Serializable;
import java.util.Map;

public interface SortableRepository<T, ID extends Serializable> extends ResourceRepository<T, ID> {
    Iterable<T> findAll(Map<String, SortingValues> sortingValuesMap);
}
