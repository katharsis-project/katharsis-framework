package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.mock.models.Pojo;

public class PojoRepository implements ResourceRepository<Pojo, Long> {

    @Override
    public Pojo findOne(Long aLong, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<Pojo> findAll(QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<Pojo> findAll(Iterable<Long> longs, QueryParams queryParams) {
        return null;
    }

    @Override
    public <S extends Pojo> S save(S entity) {
        entity.setId(1L);
        return entity;
    }

    @Override
    public void delete(Long aLong) {

    }
}
