package io.katharsis.repository.annotations;

import io.katharsis.queryParams.QueryParams;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform save operation of a resource and set of a relationship.
 * The method must be defined in a class annotated with {@link JsonApiFieldRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>A resource identifier</li>
 *     <li>Instance of a relationship value to be set</li>
 *     <li>Relationship's filed name</li>
 * </ol>
 * <p>
 * The return value should be a resource of {@link JsonApiFieldRepository#source()} type.
 * </p>
 *
 * @see io.katharsis.repository.FieldRepository#addField(Object, Object, String, QueryParams)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiAddField {
}
