package io.katharsis.legacy.repository.annotations;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.katharsis.legacy.queryParams.QueryParams;

/**
 * <p>
 * Method annotated with this annotation will be used to perform find many
 * relationship resources on a particular resource. The method must be defined
 * in a class annotated with {@link JsonApiRelationshipRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 * <li>An identifier of a source resource</li>
 * <li>Relationship's field name</li>
 * </ol>
 * <p>
 * The return value must be an {@link Iterable} of resources of
 * {@link JsonApiRelationshipRepository#target()} type.
 * </p>
 *
 * @see io.katharsis.legacy.repository.RelationshipRepository#findManyTargets(Serializable,
 *      String, QueryParams)
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindManyTargets {
}
