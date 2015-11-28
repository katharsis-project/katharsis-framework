package io.katharsis.repository.annotations;

import io.katharsis.queryParams.QueryParams;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform delete operation of resource(s) and removal of relationship(s).
 * The method must be defined in a class annotated with {@link JsonApiFieldRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>A resource identifier</li>
 *     <li>A list of relationship identifiers to be removed</li>
 *     <li>Relationship's filed name</li>
 * </ol>
 * <p>
 * The return value should be <i>void</i>.
 * </p>
 *
 * @see io.katharsis.repository.FieldRepository#deleteFields(Object, Iterable, String, QueryParams)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiDeleteFields {
}
