package io.katharsis.jackson.mock.repositories;

import io.katharsis.jackson.mock.models.ClassC;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;

public class ClassCRepository implements ResourceRepository<ClassC, Long> {
    @Override
    public ClassC findOne(Long aLong, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassC> findAll(QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassC> findAll(Iterable<Long> longs, QueryParams queryParams) {
        return null;
    }

    @Override
    public <S extends ClassC> S save(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}
