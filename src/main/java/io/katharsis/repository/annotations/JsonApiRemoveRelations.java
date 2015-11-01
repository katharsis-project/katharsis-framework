package io.katharsis.repository.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to remove relationship resources from a particular resource. The
 * method must be defined in a class annotated with {@link JsonApiRelationshipRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 *     <li>Instance of a source resource</li>
 *     <li>{@link Iterable} of relationships to be removed</li>
 *     <li>Relationship's filed name</li>
 * </ol>
 * <p>
 * The method's return value should be <i>void</i>.
 * </p>
 *
 * @see io.katharsis.repository.RelationshipRepository#removeRelations(Object, Iterable, String)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiRemoveRelations {
}
