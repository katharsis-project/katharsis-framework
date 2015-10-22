package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.exception.RepositoryNotFoundException;

import java.io.Serializable;

/**
 * Represents a non-existing repository. It is assigned to a resource class if Katharsis couldn't find any repository.
 */
public class NotFoundRepository implements ResourceRepository {

    private final Class<?> repositoryClass;

    public NotFoundRepository(Class<?> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    @Override
    public Object findOne(Serializable serializable, QueryParams queryParams) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public Iterable findAll(QueryParams queryParams) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public Iterable findAll(Iterable iterable, QueryParams queryParams) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public void delete(Serializable serializable) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public Object save(Object entity) {
        throw new RepositoryNotFoundException(repositoryClass);
    }
}
