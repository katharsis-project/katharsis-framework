package io.katharsis.repository.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * Class annotated with this annotation will be treated as a relationship repository class for a
 * {@link JsonApiFieldRepository#source()} property.
 * </p>
 * <p>
 * Repository methods defined in a class annotated by this <i>@interface</i> can throw <b>only</b> instances of
 * {@link RuntimeException}.
 * </p>
 * @see io.katharsis.repository.FieldRepository
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiFieldRepository {

    /**
     * source resource model class type
     * @return class
     */
    Class<?> source();

    /**
     * target resource model class type
     * @return class
     */
    Class<?> target();
}
