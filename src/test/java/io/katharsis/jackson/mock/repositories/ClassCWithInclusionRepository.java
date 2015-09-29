package io.katharsis.jackson.mock.repositories;

import io.katharsis.jackson.mock.models.ClassCWithInclusion;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;

public class ClassCWithInclusionRepository implements ResourceRepository<ClassCWithInclusion, Long> {
    @Override
    public ClassCWithInclusion findOne(Long aLong, RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<ClassCWithInclusion> findAll(RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<ClassCWithInclusion> findAll(Iterable<Long> longs, RequestParams requestParams) {
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
