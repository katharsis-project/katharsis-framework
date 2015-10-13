package io.katharsis.jackson.mock.repositories;

import io.katharsis.jackson.mock.models.ClassBWithInclusion;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;

public class ClassBWithInclusionRepository implements ResourceRepository<ClassBWithInclusion, Long> {
    @Override
    public ClassBWithInclusion findOne(Long aLong, RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<ClassBWithInclusion> findAll(RequestParams requestParams) {
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
