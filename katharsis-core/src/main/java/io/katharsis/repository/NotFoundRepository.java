package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.exception.RepositoryNotFoundException;

import java.io.Serializable;

/**
 * Represents a non-existing repository. It is assigned to a resource class if Katharsis couldn't find any repository.
 */
public class NotFoundRepository<T, ID extends Serializable> implements ResourceRepository<T, ID> {

    private final Class<?> repositoryClass;

    public NotFoundRepository(Class<? extends T> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    @Override
    public T findOne(ID id, QueryParams queryParams) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public Iterable<T> findAll(QueryParams queryParams) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public void delete(ID id) {
        throw new RepositoryNotFoundException(repositoryClass);
    }

    @Override
    public <S extends T> S save(S entity) {
        throw new RepositoryNotFoundException(repositoryClass);
    }
}
