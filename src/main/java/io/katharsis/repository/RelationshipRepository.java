package io.katharsis.repository;

import java.io.Serializable;

/**
 * Base unidirectional repository responsible for operations on relations. All of the methods in this interface have
 * fieldName field as last parameter. It solves a problem of many relationships between the same resources.
 *
 * @param <T> source class type
 * @param <T_ID> T class id type
 * @param <D> target class type
 */
public interface RelationshipRepository<T, T_ID extends Serializable, D> {

    int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;

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
     * @param fieldName name of source's filed
     */
    void removeRelation(T source, D target, String fieldName);

    /**
     * Find a relation's target identifier
     *
     * @param sourceId an identifier of a source
     * @param fieldName name of source's filed
     * @return an identifier of a target of a relation
     */
    D findOneTarget(T_ID sourceId, String fieldName);

    /**
     * Find a relation's target identifiers
     *
     * @param sourceId an identifier of a source
     * @param fieldName name of source's filed
     * @return identifiers of targets of a relation
     */
    Iterable<D> findTargets(T_ID sourceId, String fieldName);
}
