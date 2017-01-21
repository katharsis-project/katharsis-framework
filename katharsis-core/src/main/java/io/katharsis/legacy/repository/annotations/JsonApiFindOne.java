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
 * Method annotated with this annotation will be used to perform find one
 * operation on a particular resource. The method must be defined in a class
 * annotated with {@link JsonApiResourceRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 * <li>A resource identifier</li>
 * </ol>
 * <p>
 * The return value should be a resource of
 * {@link JsonApiResourceRepository#value()} type.
 * </p>
 *
 * @see io.katharsis.legacy.repository.ResourceRepository#findOne(Serializable,
 *      QueryParams)
 *
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindOne {
}
