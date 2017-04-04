package io.katharsis.resource.registry;

import java.util.Collection;

import io.katharsis.resource.information.ResourceInformation;

public interface ResourceRegistry {

	public RegistryEntry addEntry(Class<?> clazz, RegistryEntry entry);

	public boolean hasEntry(Class<?> clazz);

	public RegistryEntry findEntry(Class<?> resourceClass);

	public RegistryEntry getEntry(String resourceType);

	public Collection<RegistryEntry> getResources();

	public RegistryEntry findEntry(String type, Class<?> clazz);

	public ServiceUrlProvider getServiceUrlProvider();

	public String getResourceUrl(ResourceInformation resourceInformation);

	public RegistryEntry getEntryForClass(Class<?> resourceClass);
	
	/**
	 * @param resourceType
	 * @return ResourceInformation of the the top most super type of the provided resource.
	 */
	public ResourceInformation getBaseResourceInformation(String resourceType);

}
