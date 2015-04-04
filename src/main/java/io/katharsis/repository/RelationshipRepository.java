package io.katharsis.repository;

import java.io.Serializable;

/**
 * Base unidirectional repository responsible for operations on relations. All of the methods in this interface have
 * fieldName field as last parameter. It solves a problem of many relationships between the same resources.
 *
 * @param <T> source class type
 * @param <D> target class type
 */
public interface RelationshipRepository<T, T_ID extends Serializable, D, D_ID extends Serializable> {

    /**
     * Add relation from source to target
     *
     * @param source instance of a source class
     * @param target instance of a target class
     * @param fieldName name of source's filed
     */
    void addRelation(T source, D target, String fieldName);

    /**
     * Remove relation from source to target
     *
     * @param source instance of a source class
     * @param target instance of a target class
     */
    void removeRelation(T source, D target, String fieldName);

    /**
     * Find a relation's target identifier
     *
     * @param sourceId an identifier of a source
     * @return an identifier of a target of a relation
     */
    D findOneTarget(T_ID sourceId, String fieldName);

    /**
     * Find a relation's target identifiers
     *
     * @param sourceId an identifier of a source
     * @return identifiers of targets of a relation
     */
    Iterable<D> findTarget(T_ID sourceId, String fieldName);

    /**
     * Find a relation's target identifier
     *
     * @param sourceId an identifier of a source
     * @return an identifier of a target of a relation
     */
    D_ID findOneTargetId(T_ID sourceId, String fieldName);

    /**
     * Find a relation's target identifiers
     *
     * @param sourceId an identifier of a source
     * @return identifiers of targets of a relation
     */
    Iterable<D_ID> findTargetIds(T_ID sourceId, String fieldName);
}
