package io.katharsis.security;

import io.katharsis.resource.meta.MetaInformation;

/**
 * Can be implemented by MetaInformation classes to let the SecurityModule fill in the permissions.
 */
public interface ResourcePermissionInformation extends MetaInformation {

	public ResourcePermission getResourcePermission();

	/**
	 * Filled in by the {@link SecurityModule} if null.
	 * 
	 * @param resourcePermission for the requested resources.
	 */
	public void setResourcePermission(ResourcePermission resourcePermission);

}
