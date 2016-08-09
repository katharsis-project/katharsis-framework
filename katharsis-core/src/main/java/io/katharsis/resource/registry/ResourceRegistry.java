package io.katharsis.resource.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.exception.init.ResourceNotFoundInitializationException;
import io.katharsis.resource.information.ResourceInformation;

public class ResourceRegistry {
    private final Map<Class, RegistryEntry> resources = new HashMap<>();
    private final String serviceUrl;
    private final Logger logger = LoggerFactory.getLogger(ResourceRegistry.class);

    public ResourceRegistry(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Adds a new resource definition to a registry.
     * @param resource class of a resource
     * @param registryEntry resource information
     * @param <T> type of a resource
     */
    public <T> void addEntry(Class<T> resource, RegistryEntry<? extends T> registryEntry) {
        resources.put(resource, registryEntry);
        logger.debug("Added resource {} to ResourceRegistry", resource.getName());
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
     * @throws ResourceNotFoundInitializationException if resource is not found
     * @return registry entry
     */
    public RegistryEntry getEntry(Class clazz) {
    	return getEntry(clazz, false);
    }
    
    private RegistryEntry getEntry(Class clazz, boolean allowNull) {
    	Class<?> resourceClazz = clazz;
		while (!resources.containsKey(resourceClazz) && resourceClazz != Object.class) {
			resourceClazz = resourceClazz.getSuperclass();
		}
    	
        RegistryEntry registryEntry = resources.get(resourceClazz);
        if (registryEntry != null) {
            return registryEntry;
        }
        if(allowNull)
        	return null;
        else
        	throw new ResourceNotFoundInitializationException(clazz.getCanonicalName());
    }

    /**
     * Returns a JSON API resource type used by Katharsis. If a class cannot be found, <i>null</i> is returned.
     * The value is fetched from {@link ResourceInformation#getResourceType()} attribute.
     *
     * @param clazz resource class
     * @return resource type or null
     */
    public String getResourceType(Class<?> clazz) {
    	RegistryEntry<?> entry = getEntry(clazz, true);
    	if (entry == null) {
            return null;
        }
    	ResourceInformation resourceInformation = entry.getResourceInformation();
    	return resourceInformation.getResourceType();
    }
    
    public Class<?> getResourceClass(Object resource) {
    	RegistryEntry<?> entry = getEntry(resource.getClass());
    	if (entry == null) {
            throw new ResourceNotFoundException(resource.getClass().getName());
        }
    	ResourceInformation resourceInformation = entry.getResourceInformation();
    	return resourceInformation.getResourceClass();
	}

    public String getResourceUrl(Class clazz) {
    	if(serviceUrl.endsWith("/"))
    		return serviceUrl + getResourceType(clazz);
    	else
    		return serviceUrl + "/" + getResourceType(clazz);
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * Get a list of all registered resources by Katharsis.
     * @return resources
     */
    public Map<Class, RegistryEntry> getResources() {
        return Collections.unmodifiableMap(resources);
    }
}
