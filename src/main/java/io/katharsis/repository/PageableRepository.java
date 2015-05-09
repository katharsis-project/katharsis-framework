package io.katharsis.repository;

import java.io.Serializable;

public interface PageableRepository<T, ID extends Serializable> extends ResourceRepository<T, ID> {
    Iterable<T> findPage(int offset, int limit);
}
