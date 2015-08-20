package io.katharsis.resource.annotations;

import io.katharsis.resource.exception.ResourceException;

import java.lang.annotation.*;

/**
 * Indicates additional resources that should be included by default with every primary resource.
 * The field can be added to every relation defined by {@link JsonApiToOne} or {@link JsonApiToMany}. Otherwise,
 * {@link ResourceException} will be thrown at the initialization phrase.
 *
 * <p>Included resources added using this annotation are not analyzed for nested inclusion, that is only first level
 * of inclusion is provided.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiIncludeByDefault {
}
