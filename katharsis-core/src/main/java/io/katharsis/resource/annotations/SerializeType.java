package io.katharsis.resource.annotations;

/**
 * Defines the serialization strategy for a resource(s) relationship field.
 * There are two things to consider. Whether related resources should be added 
 * to the ``include`` section of the response document. And whether the id of 
 * related resources should be serialized along with the resource in the 
 * corresponding ``relationships.[name].data`` section. 
 *
 * @see JsonApiRelation
 * @since 3.0
 */
public enum SerializeType {
	/**
	 * Defines that relationship resource(s) are lazily serialized by default, meaning
	 * when explicitly requested by the ``include`` URL parameter.
	 */
	LAZY,
	/**
	 * Defines that only relationship resource(s) id(s) are serialized. 
	 * An inclusion can be requested with the the ``include`` URL parameter.
	 */
	ONLY_ID,
	/**
	 * Defines to always fully serialize relationship resource(s), both as ID and as inclusion.
	 */
	EAGER
}
