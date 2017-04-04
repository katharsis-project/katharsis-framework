package io.katharsis.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.katharsis.utils.parser.TypeParser;

/**
 * Defines a field which will be used as an identifier of a resource. It must be assigned to a field which implements
 * {@link java.io.Serializable} and {@link TypeParser} is able to parse the value.
 *
 * @see <a href="http://jsonapi.org/format/#document-structure-resource-identification">JSON API - Resource Identification</a>
 * @see TypeParser
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiId {

}
