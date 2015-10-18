package io.katharsis.repository.annotations;

import java.lang.annotation.*;

/**
 * Class annotated with this annotation will be treated as a relationship repository class for a
 * {@link JsonApiResourceRepository#value()} property.
 *
 * @see io.katharsis.repository.RelationshipRepository
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiRelationshipRepository {

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
