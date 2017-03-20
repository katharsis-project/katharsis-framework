package io.katharsis.module;

/**
 * Can be used by repositories, filters, etc. to obtain the ModuleRegistry
 * instance.
 */
public interface ModuleRegistryAware {

	public void setModuleRegistry(ModuleRegistry moduleRegistry);
}
