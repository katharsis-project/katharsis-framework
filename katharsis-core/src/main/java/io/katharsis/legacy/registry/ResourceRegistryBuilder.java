package io.katharsis.legacy.registry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.core.internal.registry.ResourceRegistryImpl;
import io.katharsis.core.internal.repository.information.ResourceRepositoryInformationImpl;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResponseRelationshipEntry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.utils.parser.TypeParser;

/**
 * Builder responsible for building an instance of ResourceRegistry.
 */
@Deprecated
public class ResourceRegistryBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRegistryBuilder.class);

	private final ResourceInformationBuilder resourceInformationBuilder;
	private final RepositoryEntryBuilderFacade repositoryEntryBuilder;

	public ResourceRegistryBuilder(ModuleRegistry moduleRegistry, JsonServiceLocator jsonServiceLocator, ResourceInformationBuilder resourceInformationBuilder) {
		this.resourceInformationBuilder = resourceInformationBuilder;
		this.repositoryEntryBuilder = new RepositoryEntryBuilderFacade(moduleRegistry, jsonServiceLocator);
		
		DefaultResourceInformationBuilderContext context = new DefaultResourceInformationBuilderContext(resourceInformationBuilder, moduleRegistry.getTypeParser());		
		resourceInformationBuilder.init(context);
	}

	/**
	 * Uses a {@link DefaultResourceLookup} to get all classes in provided
	 * package and finds all resources and repositories associated with found
	 * resource.
	 *
	 * @param packageName
	 *            Package containing resources (models) and repositories.
	 * @param serviceUrlProvider
	 *            Compute the resource to this service
	 * @return an instance of ResourceRegistry
	 */
	public ResourceRegistry build(String packageName, ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider) {
		return build(new DefaultResourceLookup(packageName), moduleRegistry, serviceUrlProvider);
	}

	/**
	 * Uses a {@link ResourceLookup} to get all resources and repositories
	 * associated with found resource.
	 *
	 * @param resourceLookup
	 *            Lookup for getting all resource classes.
	 * @param serviceUrl
	 *            URL to the service
	 * @return an instance of ResourceRegistry
	 */
	public ResourceRegistry build(ResourceLookup resourceLookup, ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrl) {
		Set<Class<?>> jsonApiResources = resourceLookup.getResourceClasses();

		Set<ResourceInformation> resourceInformationSet = new HashSet<>(jsonApiResources.size());
		

		for (Class<?> clazz : jsonApiResources) {
			resourceInformationSet.add(resourceInformationBuilder.build(clazz));
			LOGGER.trace("{} registered as a resource", clazz);
		}

		Set<RegistryEntry> registryEntries = new HashSet<>(resourceInformationSet.size());
		for (ResourceInformation resourceInformation : resourceInformationSet) {
			Class<?> resourceClass = resourceInformation.getResourceClass();

			ResourceEntry resourceEntry = repositoryEntryBuilder.buildResourceRepository(resourceLookup, resourceClass);
			LOGGER.trace("{} has a resource repository {}", resourceInformation.getResourceClass(), resourceEntry);
			List<ResponseRelationshipEntry> relationshipEntries = repositoryEntryBuilder.buildRelationshipRepositories(resourceLookup, resourceClass);
			LOGGER.trace("{} has relationship repositories {}", resourceInformation.getResourceClass(), relationshipEntries);

			ResourceRepositoryInformation repositoryInformation = new ResourceRepositoryInformationImpl(null, resourceInformation.getResourceType(), resourceInformation);
			registryEntries.add(new RegistryEntry(repositoryInformation, resourceEntry, relationshipEntries));
		}

		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(moduleRegistry, serviceUrl);
		for (RegistryEntry registryEntry : registryEntries) {
			Class<?> resourceClass = registryEntry.getResourceInformation().getResourceClass();
			RegistryEntry registryEntryParent = findParent(resourceClass, registryEntries);
			registryEntry.setParentRegistryEntry(registryEntryParent);
			resourceRegistry.addEntry(resourceClass, registryEntry);
		}

		return resourceRegistry;
	}

	/**
	 * Finds the closest resource in the class inheritance hierarchy. If no
	 * resource parent is found, <i>null</i> is returned.
	 *
	 * @param resourceClass
	 *            information about the searched resource
	 * @param registryEntries
	 *            a set of available resources
	 * @return resource's parent resource
	 */
	private RegistryEntry findParent(Class<?> resourceClass, Set<RegistryEntry> registryEntries) {
		RegistryEntry foundRegistryEntry = null;
		Class<?> currentClass = resourceClass.getSuperclass();
		classHierarchy: // goto statement?! Replace this with a recursion
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
