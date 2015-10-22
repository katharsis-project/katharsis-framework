package io.katharsis.jackson.mock.repositories;

import io.katharsis.jackson.mock.models.ClassBWithInclusion;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;

public class ClassBWithInclusionRepository implements ResourceRepository<ClassBWithInclusion, Long> {
    @Override
    public ClassBWithInclusion findOne(Long aLong, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassBWithInclusion> findAll(QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassBWithInclusion> findAll(Iterable<Long> longs, QueryParams queryParams) {
        return null;
    }

    @Override
    public <S extends ClassBWithInclusion> S save(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}
