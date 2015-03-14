package io.katharsis.repository;

/**
 * Base repository responsible for operations on relations
 *
 * @param <T> source class type
 * @param <D> destination class type
 */
public interface RelationshipRepository<T, D> {

    /**
     * Add relation from source to destination
     *
     * @param source
     * @param destination
     */
    void addRelation(T source, D destination);

    /**
     * Remove relation from source to destination
     *
     * @param source
     * @param destination
     */
    void removeRelation(T source, D destination);
}
