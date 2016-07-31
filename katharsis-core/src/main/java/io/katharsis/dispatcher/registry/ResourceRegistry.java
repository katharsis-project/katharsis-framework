package io.katharsis.dispatcher.registry;

import java.util.Map;

public interface ResourceRegistry {

    /**
     * Maps resource type (or name) to the resource class.
     */
    Map<String, Class<?>> getResources();

    /**
     * Maps the resource type to the repository that implements operations for it.
     */
    Map<String, Class<?>> getRepositories();

    /**
     * Maps resource type to the relationship repository. Resource type (source) is mapped to the target resource entry.
     * <p/>
     * (source resource) -> ( target resource , target resource class )
     */
    Map<String, Map<String, Class<?>>> getRelationships();

}
