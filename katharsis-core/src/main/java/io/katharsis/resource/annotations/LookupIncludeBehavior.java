package io.katharsis.resource.annotations;

/**
 * Defines the relationship look up strategy for a resource(s) relationship field.
 *
 * @see JsonApiRelation
 * @since 3.0
 */
public enum LookupIncludeBehavior {
	/**
	 * Defines that relationship repository is never called.
	 */
	NONE,
	/**
	 * Defines that relationship repository is called if the field is null.
	 */
	AUTOMATICALLY_WHEN_NULL,
	/**
	 * Defines that relationship repository is always called.
	 */
	AUTOMATICALLY_ALWAYS
}
