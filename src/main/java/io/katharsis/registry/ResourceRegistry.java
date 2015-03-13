package io.katharsis.registry;

import io.katharsis.resource.RestrictedResourceNames;

import java.util.HashMap;
import java.util.Map;

public class ResourceRegistry {
    private Map<String, RegistryEntry> resources = new HashMap<>();

    public void add(String resourceName, RegistryEntry registryEntry) {
        if (RestrictedResourceNames.valueOf(resourceName) != null) {
            throw new IllegalArgumentException("Resource value is restricted: " + resourceName);
        }
        resources.put(resourceName, registryEntry);
    }

    public RegistryEntry get(String resourceName) {
        return resources.get(resourceName);
    }
}
