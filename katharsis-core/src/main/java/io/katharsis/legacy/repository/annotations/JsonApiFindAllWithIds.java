package io.katharsis.legacy.repository.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.katharsis.legacy.queryParams.QueryParams;

/**
 * Method annotated with this annotation will be used to perform find all
 * operation constrained by a list of identifiers. The method must be defined in
 * a class annotated with {@link JsonApiResourceRepository}.
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 * <li>An {@link Iterable} of resource identifiers</li>
 * </ol>
 * <p>
 * The return value must be an {@link Iterable} of resources of
 * {@link JsonApiResourceRepository#value()} type.
 *
 * @see io.katharsis.legacy.repository.ResourceRepository#findAll(Iterable,
 *      QueryParams)
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindAllWithIds {
}
