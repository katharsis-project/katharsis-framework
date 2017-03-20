package io.katharsis.resource.registry;

/**
 * Can be used by repositories, filters, etc. to obtain a ResourceRegistry instance.
 */
public interface ResourceRegistryAware {

	public void setResourceRegistry(ResourceRegistry resourceRegistry);
}
