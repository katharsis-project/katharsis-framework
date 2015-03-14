package io.katharsis.repository;

import java.io.Serializable;

/**
 * Base repository which is used to operate on the entities
 *
 * @param <T>  Type of an entity
 * @param <ID> Type of Identifier of an entity
 */
public interface EntityRepository<T, ID extends Serializable> {

    <S extends T> S save(S entity);

    T findOne(ID id);

    Iterable<T> findAll();

    void delete(ID id);
}
