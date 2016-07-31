package io.katharsis.resource.registry;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.init.ResourceNotFoundInitializationException;
import io.katharsis.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ResourceRegistry {

    private final Map<Class, RegistryEntry> resources = new HashMap<>();
    private final String serviceUrl;

    public ResourceRegistry(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Adds a new resource definition to a registry.
     *
     * @param resource      class of a resource
     * @param registryEntry resource information
     * @param <T>           type of a resource
     */
    public <T> void addEntry(Class<T> resource, RegistryEntry<? extends T> registryEntry) {
        resources.put(resource, registryEntry);
        log.debug("Added resource {} to ResourceRegistry", resource.getName());
    }

    /**
     * Searches the registry for a resource identified by a JSON API resource type.
     * If a resource cannot be found, <i>null</i> is returned.
     *
     * @param searchType resource type
     * @return registry entry or <i>null</i>
     */
    public RegistryEntry getEntry(String searchType) {
        for (Map.Entry<Class, RegistryEntry> entry : resources.entrySet()) {
            String type = getResourceType(entry.getKey());
            if (type.equals(searchType)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Searches the registry for a resource identified by a JSON API resource class.
     * If a resource cannot be found, {@link ResourceNotFoundInitializationException} is thrown.
     *
     * @param clazz resource type
     * @return registry entry
     * @throws ResourceNotFoundInitializationException if resource is not found
     */
    public RegistryEntry getEntry(Class clazz) {
        Class resourceClazz = ClassUtils.getJsonApiResourceClass(clazz);
        if (resourceClazz == null) {
            throw new ResourceNotFoundInitializationException(clazz.getCanonicalName());
        }
        RegistryEntry registryEntry = resources.get(resourceClazz);
        if (registryEntry != null) {
            return registryEntry;
        }
        throw new ResourceNotFoundInitializationException(clazz.getCanonicalName());
    }

    /**
     * Returns a JSON API resource type used by Katharsis. If a class cannot be found, <i>null</i> is returned.
     * The value is fetched from {@link JsonApiResource#type()} attribute.
     *
     * @param clazz resource class
     * @return resource type or null
     */
    public String getResourceType(Class clazz) {
        Class resourceClazz = ClassUtils.getJsonApiResourceClass(clazz);
        if (resourceClazz == null) {
            return null;
        }
        Annotation[] annotations = resourceClazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof JsonApiResource) {
                JsonApiResource apiResource = (JsonApiResource) annotation;
                return apiResource.type();
            }
        }
        // won't reach this
        return null;
    }

    public String getResourceUrl(Class clazz) {
        return serviceUrl + "/" + getResourceType(clazz);
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * Get a list of all registered resources by Katharsis.
     *
     * @return resources
     */
    public Map<Class, RegistryEntry> getResources() {
        return Collections.unmodifiableMap(resources);
    }
}
