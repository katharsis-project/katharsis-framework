package io.katharsis.resource.registry;

import io.katharsis.resource.annotations.JsonApiResource;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class ResourceRegistry {
    private Map<Class, RegistryEntry> resources = new HashMap<>();

    public <T> void add(Class<T> resource, RegistryEntry<? extends T> registryEntry) {
        resources.put(resource, registryEntry);
    }

    public RegistryEntry get(String searchType) {
        for (Map.Entry<Class, RegistryEntry> entry : resources.entrySet()) {
            String type = getResourceType(entry.getKey());
            if (searchType.equals(type)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("Wrong search type");
    }

    private String getResourceType(Class clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof JsonApiResource) {
                JsonApiResource apiResource = (JsonApiResource) annotation;
                return apiResource.type();
            }
        }
        return null;
    }
}
