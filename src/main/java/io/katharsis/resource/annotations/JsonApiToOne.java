package io.katharsis.resource.annotations;

import java.lang.annotation.*;

/**
 * Indicates an association to single value which need to be handled by a separate resource.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonApiToOne {
}
