package io.katharsis.core.internal.jackson.mock.repositories;

import io.katharsis.core.internal.jackson.mock.models.ClassAWithInclusion;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.ResourceRepository;

public class ClassAWithInclusionRepository implements ResourceRepository<ClassAWithInclusion, Long> {
    @Override
    public ClassAWithInclusion findOne(Long aLong, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassAWithInclusion> findAll(QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<ClassAWithInclusion> findAll(Iterable<Long> longs, QueryParams queryParams) {
        return null;
    }

    @Override
    public <S extends ClassAWithInclusion> S save(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}
