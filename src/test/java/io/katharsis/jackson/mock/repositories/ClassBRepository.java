package io.katharsis.jackson.mock.repositories;

import io.katharsis.jackson.mock.models.ClassB;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;

public class ClassBRepository implements ResourceRepository<ClassB, Long> {
    @Override
    public ClassB findOne(Long aLong, RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<ClassB> findAll(RequestParams requestParams) {
        return null;
    }

    @Override
    public <S extends ClassB> S save(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}
