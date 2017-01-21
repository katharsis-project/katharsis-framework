package io.katharsis.resource.registry;

/**
 * Identifies a relationship repository entry
 */
public interface ResponseRelationshipEntry<T, D> {

    /**
     * @return target class
     */
    Class<?> getTargetAffiliation();
}
