package io.katharsis.repository;

import java.io.Serializable;

public interface PageableRepository<T, ID extends Serializable> extends ResourceRepository<T, ID> {
    Iterable<T> findPage(int offset, int limit);

    Iterable<T> first();

    Iterable<T> last();

    Iterable<T> next();

    Iterable<T> previousOrFirst();
}
