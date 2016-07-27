package io.katharsis.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an association to many values which need to be handled by a separate repository.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiToMany {

    /**
     * Defines weather the data associated to the relation should be visible when requesting information about a
     * resource that contains this relation.
     * @return <i>true</i> if lazy, <i>false</i> otherwise
     */
    boolean lazy() default true;
}
