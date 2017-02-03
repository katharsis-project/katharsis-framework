package io.katharsis.resource.registry;

/**
 * Identifies a relationship repository entry
 */
public interface ResponseRelationshipEntry {

    /**
     * @return target class
     */
    Class<?> getTargetAffiliation();
}
