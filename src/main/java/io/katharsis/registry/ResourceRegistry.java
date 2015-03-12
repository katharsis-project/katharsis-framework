package io.katharsis.registry;

import java.util.HashMap;
import java.util.Map;

public class ResourceRegistry {
    private Map<String, RegistryEntry> resources = new HashMap<>();

    public void add(String resourceName, RegistryEntry registryEntry) {
        resources.put(resourceName, registryEntry);
    }

    public RegistryEntry get(String resourceName) {
        return resources.get(resourceName);
    }
}
