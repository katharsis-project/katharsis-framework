package io.katharsis.resource.annotations;

import static io.katharsis.resource.annotations.LookupIncludeBehavior.NONE;
import static io.katharsis.resource.annotations.SerializeType.LAZY;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an association to either a single value or collection which needs to be handled by a separate
 * relationship repository.
 *
 * @since 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiRelation {

	/**
	 * (Optional) Defines whether the data associated to the relationship should be serialized when making a request.
	 *
	 * LAZY (Default) - is serialize the relationship when requested with an include query parameter.
	 *
	 * ONLY_ID - is only serialize the ids in the resources relationship section but not the included section.
	 *
	 * EAGER - is always serialize this relationship.
	 */
	SerializeType serialize() default LAZY;

	/**
	 * (Optional) This attribute is used to make automatic value assignment using a defined relationship repository if such repository
	 * is available.
	 *
	 * NONE (Default) - do not automatically call this fields relationship findManyTargets or findOneTarget.
	 *
	 * AUTOMATICALLY_WHEN_NULL - automatically perform a relationship findManyTargets or findOneTarget when this field's value
	 * is null and it is either A. requested in an include query parameter B. SerializeType.ONLY_ID or SerializeType.EAGER
	 * is present.
	 *
	 * AUTOMATICALLY_ALWAYS - always automatically call a relationship's findManyTargets or findOneTarget and overwrite this field.
	 */
	LookupIncludeBehavior lookUp() default NONE;

	/**
	 * @return opposite attribute name in case of a bidirectional association. Used by {@link io.katharsis.repository.RelationshipRepositoryBase} to implement
	 * its findOneTarget and findManyTarget functions by directly searching in the related resource repository with a filter in the opposite direction.
	 * Allow to work with relations with only implementing resource repositories!
	 */
	String opposite() default "";
}
