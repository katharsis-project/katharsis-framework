package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.NotFoundRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.repository.DirectResourceEntry;
import io.katharsis.resource.registry.repository.RelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import net.jodah.typetools.TypeResolver;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builder responsible for building an instance of ResourceRegistry.
 */
public class ResourceRegistryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRegistryBuilder.class);

    private final JsonServiceLocator jsonServiceLocator;
    private final ResourceInformationBuilder resourceInformationBuilder;

    private final RepositoryEntryBuilderFacade repositoryEntryBuilder;

    public ResourceRegistryBuilder(JsonServiceLocator jsonServiceLocator, ResourceInformationBuilder resourceInformationBuilder) {
        this.jsonServiceLocator = jsonServiceLocator;
        this.resourceInformationBuilder = resourceInformationBuilder;
        this.repositoryEntryBuilder = new RepositoryEntryBuilderFacade(jsonServiceLocator);
    }

    /**
     * Scans all classes in provided package and finds all resources and repositories associated with found resource.
     *
     * @param packageName Package containing resources (models) and repositories.
     * @param serviceUrl  URL to the service
     * @return an instance of ResourceRegistry
     */
    public ResourceRegistry build(String packageName, @SuppressWarnings("SameParameterValue") String serviceUrl) {
        Reflections reflections;
        if (packageName != null) {
            String[] packageNames = packageName.split(",");
            reflections = new Reflections(packageNames);
        } else {
            reflections = new Reflections(packageName);
        }


        Set<Class<?>> jsonApiResources = reflections.getTypesAnnotatedWith(JsonApiResource.class);
        Set<ResourceInformation> resourceInformationSet = jsonApiResources.stream()
            .map(resourceInformationBuilder::build)
            .collect(Collectors.toSet());

        Set<RegistryEntry> registryEntries = new HashSet<>(resourceInformationSet.size());
        for (ResourceInformation resourceInformation : resourceInformationSet) {
            Class<?> resourceClass = resourceInformation.getResourceClass();

            ResourceEntry<?, ?> resourceEntry = repositoryEntryBuilder.buildResourceRepository(reflections, resourceClass);
            List<RelationshipEntry<?, ?>> relationshipEntries = repositoryEntryBuilder
            .buildRelationshipRepositories(reflections, resourceClass);


            RegistryEntry registryEntry = new RegistryEntry(resourceInformation, resourceEntry, relationshipEntries);

            registryEntries.add(registryEntry);

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

    private List<RelationshipRepository> initializeRelationshipRepositories(
        Set<Class<? extends RelationshipRepository>> foundRelationshipRepositoriesClasses, Class resourceClass) {
        List<RelationshipRepository> relationshipRepositories = new LinkedList<>();
        for (Class<? extends RelationshipRepository> relationshipRepositoryClass : foundRelationshipRepositoriesClasses) {
            RelationshipRepository relationshipRepository = jsonServiceLocator.getInstance(relationshipRepositoryClass);
            if (relationshipRepository == null) {
                throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
            }

            LOGGER.debug("Assigned {} RelationshipRepository  to {} resource class",
                relationshipRepositoryClass.getCanonicalName(), resourceClass.getCanonicalName());

            relationshipRepositories.add(relationshipRepository);
        }
        return relationshipRepositories;
    }
}
