package io.katharsis.repository.annotations;

import io.katharsis.queryParams.RequestParams;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform find all operation constrained by a list of
 * identifiers. The method must be defined in a class annotated with {@link JsonApiResourceRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>An {@link Iterable} of resource identifiers</li>
 * </ol>
 * <p>
 * <p>
 * The return value must be an {@link Iterable} of resources of {@link JsonApiResourceRepository#value()} type.
 * </p>
 *
 * @see io.katharsis.repository.ResourceRepository#findAll(Iterable, RequestParams)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindAllWithIds {
}
