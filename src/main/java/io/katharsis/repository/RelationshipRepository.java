package io.katharsis.repository;

/**
 * Base unidirectional repository responsible for operations on relations.
 *
 * @param <T> source class type
 * @param <D> destination class type
 */
public interface RelationshipRepository<T, D> {

    /**
     * Add relation from source to destination
     *
     * @param source instance of a source class
     * @param destination instance of a destination class
     */
    void addRelation(T source, D destination);

    /**
     * Remove relation from source to destination
     *
     * @param source instance of a source class
     * @param destination instance of a destination class
     */
    void removeRelation(T source, D destination);
}
