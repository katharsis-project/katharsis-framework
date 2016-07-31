package io.katharsis.dispatcher.registry;

import lombok.Value;

import java.util.Map;

@Value
public class RegistryDataHolder implements ResourceRegistry {

    /**
     * Maps resource type (or name) to the resource class.
     */
    private Map<String, Class<?>> resources;
    /**
     * Maps the resource type to the repository that implements operations for it.
     */
    private Map<String, Class<?>> repositories;
    /**
     * Maps resource type to the relationship repository. Resource type (source) is mapped to the target resource entry.
     * <p/>
     * (source resource) -> ( target resource , target resource class )
     */
    private Map<String, Map<String, Class<?>>> relationships;

}
