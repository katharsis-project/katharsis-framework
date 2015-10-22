package io.katharsis.repository.annotations;

import java.lang.annotation.*;

/**
 * Class annotated with this annotation will be treated as a repository class for a
 * {@link JsonApiResourceRepository#value()} property.
 *
 * @see io.katharsis.repository.ResourceRepository
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiResourceRepository {
    Class<?> value();
}
