package io.katharsis.repository;

import java.io.Serializable;

/**
 * Base repository which is used to operate on the entities
 * @param <T> Type of an entity
 * @param <ID> Type of Identifier of an entity
 */
public interface JsonRepository<T, ID extends Serializable> {
    
    <S extends T> S save(S entity);
    
    T findOne(ID id);
    
    Iterable<T> findAll(Iterable<ID> ids);
    
    void delete(ID id);
    
    <D> void addRelation(T source, D destination);
    
    <D> void removeRelation(T source, D destination);
}
