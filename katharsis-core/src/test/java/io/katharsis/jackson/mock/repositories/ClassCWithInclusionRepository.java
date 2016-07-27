package io.katharsis.jackson.mock.repositories;

import io.katharsis.jackson.mock.models.ClassCWithInclusion;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;

public class ClassCWithInclusionRepository implements ResourceRepository<ClassCWithInclusion, Long> {
    @Override
    public ClassCWithInclusion findOne(Long aLong, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassCWithInclusion> findAll(QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassCWithInclusion> findAll(Iterable<Long> longs, QueryParams queryParams) {
        return null;
    }

    @Override
    public <S extends ClassCWithInclusion> S save(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}
