package io.katharsis.dispatcher.controller;

import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;

public class Utils {

    /**
     * TODO: ieugen: this might be better placed inside resourceRegistry.getEntry(resourceName);
     *
     * @param registryEntry
     * @param resourceName
     */
    public static RegistryEntry checkResourceExists(RegistryEntry registryEntry, String resourceName) {
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        return registryEntry;
    }

    public static void checkResourceFieldExists(ResourceField relationshipField, String resourceName) {
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(resourceName);
        }
    }

}
