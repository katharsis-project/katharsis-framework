package io.katharsis.resource.registry;

import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.init.ResourceNotFoundInitializationException;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.utils.java.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ResourceRegistry {
    private final Map<Class, RegistryEntry> resources;
    private final ServiceUrlProvider serviceUrlProvider;
    private final Logger logger = LoggerFactory.getLogger(ResourceRegistry.class);
    private ModuleRegistry moduleRegistry;

    public ResourceRegistry(ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider) {
        this.moduleRegistry = moduleRegistry;
        this.serviceUrlProvider = serviceUrlProvider;
        this.resources = new HashMap<>();
        this.moduleRegistry.setResourceRegistry(this);
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
        registryEntry.initialize(moduleRegistry);
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
            if (type == null) {
                return null;
            }
            if (type.equals(searchType)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Searches the registry for a resource identified by,
     * 1) JSON API resource type.
     * 2) JSON API resource class.
     * <p>
     * If a resource cannot be found, {@link ResourceNotFoundInitializationException} is thrown.
     *
     * @param searchType resource type
     * @param clazz      resource type
     * @return registry entry
     * @throws ResourceNotFoundInitializationException if resource is not found
     */
    public <T> RegistryEntry<T> getEntry(String searchType, Class<T> clazz) {
        RegistryEntry<T> entry = getEntry(searchType);
        if (entry == null) {
            return getEntry(clazz, false);
        }
        return entry;
    }

    /**
     * Searches the registry for a resource identified by a JSON API resource class.
     * If a resource cannot be found, {@link ResourceNotFoundInitializationException} is thrown.
     *
     * @param clazz resource type
     * @return registry entry
     * @throws ResourceNotFoundInitializationException if resource is not found
     */
    public <T> RegistryEntry<T> getEntry(Class<T> clazz) {
        return (RegistryEntry<T>) getEntry(clazz, false);
    }

    public boolean hasEntry(Class<?> clazz) {
        return getEntry(clazz, true) != null;
    }

    protected <T> RegistryEntry<T> getEntry(Class<T> clazz, boolean allowNull) {
        Optional<Class<?>> resourceClazz = getResourceClass(clazz);
        if (allowNull && !resourceClazz.isPresent())
            return null;
        else if (!resourceClazz.isPresent())
            throw new ResourceNotFoundInitializationException(clazz.getCanonicalName());
        return resources.get(resourceClazz.get());
    }

    public <T> RegistryEntry<T> getEntry(T targetDataObject) {
        Class<?> targetDataObjClass = targetDataObject.getClass();
        RegistryEntry relationshipEntry;
        if (targetDataObjClass.getAnnotation(JsonApiResource.class) != null) {
            relationshipEntry = getEntry(targetDataObjClass.getAnnotation(JsonApiResource.class).type(),
                    targetDataObjClass.getClass());
        } else {
            relationshipEntry = getEntry(targetDataObject.getClass());
        }
        return relationshipEntry;
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
        if (resourceInformation == null) {
            return null;
        }
        return resourceInformation.getResourceType();
    }

    public Optional<Class<?>> getResourceClass(Object resource) {
        return getResourceClass(resource.getClass());
    }

    public Optional<Class<?>> getResourceClass(Class<?> resourceClass) {
        Class<?> currentClass = resourceClass;
        while (currentClass != null && currentClass != Object.class) {
            RegistryEntry<?> entry = resources.get(currentClass);
            if (entry != null) {
                return (Optional) Optional.of(currentClass);
            }
            currentClass = currentClass.getSuperclass();
        }
        return Optional.empty();
    }

    public String getResourceUrl(Class<?> clazz) {
        return serviceUrlProvider.getUrl() + "/" + getResourceType(clazz);
    }

    public String getServiceUrl() {
        return serviceUrlProvider.getUrl();
    }

    public ServiceUrlProvider getServiceUrlProvider() {
        return serviceUrlProvider;
    }

    /**
     * Get a list of all registered resources by Katharsis.
     *
     * @return resources
     */
    public Map<Class<?>, RegistryEntry<?>> getResources() {
        return (Map) Collections.unmodifiableMap(resources);
    }
}
