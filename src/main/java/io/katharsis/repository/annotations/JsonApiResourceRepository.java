package io.katharsis.repository.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * Class annotated with this annotation will be treated as a repository class for a
 * {@link JsonApiResourceRepository#value()} property.
 * </p>
 * <p>
 * Repository methods defined in a class annotated by this <i>@interface</i> can throw <b>only</b> instances of
 * {@link RuntimeException}.
 * </p>
 * @see io.katharsis.repository.ResourceRepository
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiResourceRepository {
    Class<?> value();
}
