package io.katharsis.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.katharsis.resource.annotations.JsonApiResource;

/**
 * Allows to specify a resource type for an exposed entity. This annotation is
 * optional. By default the resource type is derived from the entity name (e.g.
 * Person => person).
 * 
 * The annotation corresponds to the default {@link JsonApiResource} annotation,
 * but with the additional benefit of reading JPA annotations to detect primary
 * keys, relationships, etc. without having to define redudant Katharsis
 * annotations.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Deprecated // currently not supported
public @interface JpaResource {

	/**
	 * Defines the type of the resource.
	 */
	String type();
}
