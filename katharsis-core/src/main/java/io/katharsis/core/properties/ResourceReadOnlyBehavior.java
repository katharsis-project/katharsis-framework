package io.katharsis.core.properties;

/**
 * Determines how to deal with read-only fields upon a PATCH or POST of a resource. See also
 * {@link JsonApiField} for more information.
 */
public enum ResourceReadOnlyBehavior {
	
	/**
	 * Ignores attribute in POST and PATCH requests that cannot be changed. This is the default.
	 */
	IGNORE,
	
	/**
	 * Throws a {@link BadRequestException} when attempting to change an attribute with a 
	 * POST and PATCH request that cannot be changed.
	 */
	FAIL

}
