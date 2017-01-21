package io.katharsis.core.internal.jackson.mock.repositories;

import io.katharsis.core.internal.jackson.mock.models.ClassA;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.ResourceRepository;

public class ClassARepository implements ResourceRepository<ClassA, Long> {
    @Override
    public ClassA findOne(Long aLong, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassA> findAll(QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassA> findAll(Iterable<Long> longs, QueryParams queryParams) {
        return null;
    }

    @Override
    public <S extends ClassA> S save(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}
