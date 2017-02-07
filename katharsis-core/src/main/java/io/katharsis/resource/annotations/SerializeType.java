package io.katharsis.resource.annotations;

/**
 * Defines the serialization strategy for a resource(s) relationship field.
 *
 * @see JsonApiRelation
 * @since 3.0
 */
public enum SerializeType {
	/**
	 * Defines that relationship resource(s) are lazily serialized by default.
	 */
	LAZY,
	/**
	 * Defines that only relationship resource(s) id(s) are serialized.
	 */
	ONLY_ID,
	/**
	 * Defines to always serialize relationship resource(s)
	 */
	EAGER
}
