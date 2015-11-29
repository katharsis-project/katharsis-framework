package io.katharsis.repository.annotations;

import io.katharsis.queryParams.QueryParams;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform add operation of resource(s) and set of relationship(s).
 * The method must be defined in a class annotated with {@link JsonApiFieldRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>A resource identifier</li>
 *     <li>A list of instances of a relationship values to be set</li>
 *     <li>Relationship's filed name</li>
 * </ol>
 * <p>
 * The return value should be a resource of {@link JsonApiFieldRepository#source()} type.
 * </p>
 *
 * @see io.katharsis.repository.FieldRepository#addFields(Object, Iterable, String, QueryParams)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiAddFields {
}
