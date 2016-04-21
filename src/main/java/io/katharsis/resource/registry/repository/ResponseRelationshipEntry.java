package io.katharsis.resource.registry.repository;

/**
 * Identifies a relationship repository entry
 */
public interface ResponseRelationshipEntry<T, D> {

    /**
     * @return target class
     */
    Class<?> getTargetAffiliation();
}
