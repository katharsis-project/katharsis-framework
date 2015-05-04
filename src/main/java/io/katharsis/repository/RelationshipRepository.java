package io.katharsis.repository;

import java.io.Serializable;

/**
 * Base unidirectional repository responsible for operations on relations. All of the methods in this interface have
 * fieldName field as last parameter. It solves a problem of many relationships between the same resources.
 *
 * @param <T> source class type
 * @param <T_ID> T class id type
 * @param <D> target class type
 * @param <D_ID> D class id type
 */
public interface RelationshipRepository<T, T_ID extends Serializable, D, D_ID extends Serializable> {

    int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;

    /**
     * Set a relation defined by a field. targetId parameter can be either in a form of an object or null value,
     * which means that if there's a relation, it should be removed.
     *
     * @param source instance of a source class
     * @param targetId id of a target resource
     * @param fieldName name of target's filed
     */
    void setRelation(T source, D_ID targetId, String fieldName);

    /**
     * Set a relation defined by a field. targetIds parameter can be either in a form of an object or null value,
     * which means that if there's a relation, it should be removed.
     *
     * @param source instance of a source class
     * @param targetIds ids of a target resource
     * @param fieldName name of target's filed
     */
    void setRelations(T source, Iterable<D_ID> targetIds, String fieldName);

    /**
     * Find a relation's target identifier
     *
     * @param sourceId an identifier of a source
     * @param fieldName name of target's filed
     * @return an identifier of a target of a relation
     */
    D findOneTarget(T_ID sourceId, String fieldName);

    /**
     * Find a relation's target identifiers
     *
     * @param sourceId an identifier of a source
     * @param fieldName name of target's filed
     * @return identifiers of targets of a relation
     */
    Iterable<D> findTargets(T_ID sourceId, String fieldName);
}
