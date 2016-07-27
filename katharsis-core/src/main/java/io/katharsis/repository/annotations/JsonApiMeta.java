package io.katharsis.repository.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Method annotated with this annotation will be used to provide meta information for a set of resources. The method
 * must be defined in a class annotated with {@link JsonApiResourceRepository} or
 * {@link JsonApiRelationshipRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>A list of resources</li>
 * </ol>
 * <p>
 * The return value must be an instance of {@link io.katharsis.response.MetaInformation} type.
 * </p>
 *
 * @see io.katharsis.repository.MetaRepository
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiMeta {
}
