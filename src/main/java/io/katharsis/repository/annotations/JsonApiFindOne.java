package io.katharsis.repository.annotations;

import java.lang.annotation.*;

/**
 * Annotated method should have at least one parameter, that is, the first parameter should be an id value.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindOne {
}
