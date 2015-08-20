package io.katharsis.resource.annotations;

import java.lang.annotation.*;

/**
 * Indicates an association to many values which need to be handled by a separate repository.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiToMany {
}
