package io.katharsis.repository.annotations;

import io.katharsis.queryParams.QueryParams;

import java.io.Serializable;
import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform find one relationship resource on a particular
 * resource. The method must be defined in a class annotated with {@link JsonApiRelationshipRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>An identifier of a source resource</li>
 *     <li>Relationship's field name</li>
 * </ol>
 * <p>
 * The return value must be a resources of {@link JsonApiRelationshipRepository#target()} type.
 * </p>
 *
 * @see io.katharsis.repository.RelationshipRepository#findOneTarget(Serializable, String, QueryParams)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindOneTarget {
}
