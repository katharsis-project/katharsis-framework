package io.katharsis.locator;

import io.katharsis.dispatcher.registry.api.Repository;
import io.katharsis.query.QueryParams;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;


@Slf4j
public class AnnotationBasedRepositoryWrapper implements Repository {

    @Override
    public Object findOne(Serializable serializable, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable findAll(QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable findAll(Iterable iterable, QueryParams queryParams) {
        return null;
    }

    @Override
    public void delete(Serializable serializable) {

    }

    @Override
    public Object save(Object entity) {
        return null;
    }
}
