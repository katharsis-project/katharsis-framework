package io.katharsis.repository.annotations;

import io.katharsis.queryParams.QueryParams;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform delete operation of a resource and removal of a relationship.
 * The method must be defined in a class annotated with {@link JsonApiFieldRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>A resource identifier</li>
 *     <li>Relationship's filed name</li>
 * </ol>
 * <p>
 * The return value should be <i>void</i>.
 * </p>
 *
 * @see io.katharsis.repository.FieldRepository#deleteField(Object, String, QueryParams)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiDeleteField {
}
