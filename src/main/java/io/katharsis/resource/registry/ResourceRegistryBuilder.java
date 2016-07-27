package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.repository.ResourceEntry;
import io.katharsis.resource.registry.repository.ResponseRelationshipEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builder responsible for building an instance of ResourceRegistry.
 */
public class ResourceRegistryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRegistryBuilder.class);

    private final ResourceInformationBuilder resourceInformationBuilder;
    private final RepositoryEntryBuilderFacade repositoryEntryBuilder;

    public ResourceRegistryBuilder(JsonServiceLocator jsonServiceLocator, ResourceInformationBuilder resourceInformationBuilder) {
        this.resourceInformationBuilder = resourceInformationBuilder;
        this.repositoryEntryBuilder = new RepositoryEntryBuilderFacade(jsonServiceLocator);
    }

    /**
     * Uses a {@link DefaultResourceLookup} to get all  classes in provided package and finds all resources and repositories associated with found resource.
     *
     * @param packageName Package containing resources (models) and repositories.
     * @param serviceUrl  URL to the service
     * @return an instance of ResourceRegistry
     */
    public ResourceRegistry build(String packageName, @SuppressWarnings("SameParameterValue") String serviceUrl) {
    	return build(new DefaultResourceLookup(packageName), serviceUrl);
    }

    /**
     * Uses a {@link ResourceLookup} to get all resources and repositories associated with found resource.
     *
     * @param resourceLookup Lookup for getting all resource classes.
     * @param serviceUrl  URL to the service
     * @return an instance of ResourceRegistry
     */
    public ResourceRegistry build(ResourceLookup resourceLookup, @SuppressWarnings("SameParameterValue") String serviceUrl) {
        Set<Class<?>> jsonApiResources = resourceLookup.getResourceClasses();
        
        Set<ResourceInformation> resourceInformationSet = new HashSet<>(jsonApiResources.size());
        for (Class<?> clazz : jsonApiResources) {
            resourceInformationSet.add(resourceInformationBuilder.build(clazz));
            LOGGER.info("{} registered as a resource", clazz);
        }

        Set<RegistryEntry> registryEntries = new HashSet<>(resourceInformationSet.size());
        for (ResourceInformation resourceInformation : resourceInformationSet) {
            Class<?> resourceClass = resourceInformation.getResourceClass();

            ResourceEntry<?, ?> resourceEntry = repositoryEntryBuilder.buildResourceRepository(resourceLookup, resourceClass);
            LOGGER.info("{} has a resource repository {}", resourceInformation.getResourceClass(), resourceEntry);
            List<ResponseRelationshipEntry<?, ?>> relationshipEntries = repositoryEntryBuilder
            .buildRelationshipRepositories(resourceLookup, resourceClass);
            LOGGER.info("{} has relationship repositories {}", resourceInformation.getResourceClass(), relationshipEntries);

            registryEntries.add(new RegistryEntry(resourceInformation, resourceEntry, relationshipEntries));

        }

        ResourceRegistry resourceRegistry = new ResourceRegistry(serviceUrl);
        for (RegistryEntry registryEntry : registryEntries) {
            Class<?> resourceClass = registryEntry.getResourceInformation().getResourceClass();
            RegistryEntry registryEntryParent = findParent(resourceClass, registryEntries);
            registryEntry.setParentRegistryEntry(registryEntryParent);
            resourceRegistry.addEntry(resourceClass, registryEntry);
        }

        return resourceRegistry;
    }

    /**
     * Finds the closest resource, that is resource annotated with {@link JsonApiResource} annotation, in the class
     * inheritance hierarchy. If no resource parent is found, <i>null</i> is returned.
     *
     * @param resourceClass    information about the searched resource
     * @param registryEntries a set of available resources
     * @return resource's parent resource
     */
    private RegistryEntry findParent(Class<?> resourceClass, Set<RegistryEntry> registryEntries) {
        RegistryEntry foundRegistryEntry = null;
        Class<?> currentClass = resourceClass.getSuperclass();
        classHierarchy:
        while (currentClass != null && currentClass != Object.class) {
            for (RegistryEntry availableRegistryEntry : registryEntries) {
                if (availableRegistryEntry.getResourceInformation().getResourceClass().equals(currentClass)) {
                    foundRegistryEntry = availableRegistryEntry;
                    break classHierarchy;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return foundRegistryEntry;
    }
}
