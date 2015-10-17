package io.katharsis.jackson.mock.repositories;

import io.katharsis.jackson.mock.models.ClassAWithInclusion;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;

public class ClassAWithInclusionRepository implements ResourceRepository<ClassAWithInclusion, Long> {
    @Override
    public ClassAWithInclusion findOne(Long aLong, RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<ClassAWithInclusion> findAll(RequestParams requestParams) {
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
