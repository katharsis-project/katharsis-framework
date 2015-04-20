package io.katharsis.repository;

import java.io.Serializable;

/**
 * Base repository which is used to operate on the entities
 *
 * @param <T>  Type of an entity
 * @param <ID> Type of Identifier of an entity
 */
public interface ResourceRepository<T, ID extends Serializable> {

    T findOne(ID id);

    Iterable<T> findAll();

    <S extends T> S save(S entity);

    <S extends T> S update(S entity);

    void delete(ID id);
}
