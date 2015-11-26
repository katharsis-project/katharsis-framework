package io.katharsis.resource.registry;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.init.ResourceNotFoundInitializationException;
import io.katharsis.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class ResourceRegistry {
    private final Map<Class, RegistryEntry> resources = new HashMap<>();
    private final String serviceUrl;
    private final Logger logger = LoggerFactory.getLogger(ResourceRegistry.class);

    public ResourceRegistry(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }


    public <T> void addEntry(Class<T> resource, RegistryEntry<? extends T> registryEntry) {
        resources.put(resource, registryEntry);
        logger.debug("Added resource {} to ResourceRegistry", resource.getName());
    }

    public RegistryEntry getEntry(String searchType) {
        RegistryEntry registryEntry = null;
        for (Map.Entry<Class, RegistryEntry> entry : resources.entrySet()) {
            String type = getResourceType(entry.getKey());
            if (type.equals(searchType)) {
                registryEntry = entry.getValue();
            }
        }
        return registryEntry;
    }

    public RegistryEntry getEntry(Class clazz) {
        Class resourceClazz = ClassUtils.getJsonApiResourceClass(clazz);
        RegistryEntry registryEntry = resources.get(resourceClazz);
        if (registryEntry != null) {
            return registryEntry;
        }
        throw new ResourceNotFoundInitializationException(clazz.getCanonicalName());
    }

    public String getResourceType(Class clazz) {
        Class resourceClazz = ClassUtils.getJsonApiResourceClass(clazz);
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
}
