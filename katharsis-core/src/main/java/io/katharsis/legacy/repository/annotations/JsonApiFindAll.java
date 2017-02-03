package io.katharsis.legacy.repository.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.katharsis.legacy.queryParams.QueryParams;

/**
 * <p>
 * Method annotated with this annotation will be used to perform find all
 * operation. The method must be defined in a class annotated with
 * {@link JsonApiResourceRepository}.
 * </p>
 * <p>
 * There are no requirements on the method parameters.
 * </p>
 * <p>
 * The return value must be an {@link Iterable} of resources of
 * {@link JsonApiResourceRepository#value()} type.
 * </p>
 *
 * @see io.katharsis.legacy.repository.ResourceRepository#findAll(QueryParams)
 *
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindAll {
}
