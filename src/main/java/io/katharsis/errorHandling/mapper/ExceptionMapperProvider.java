package io.katharsis.errorHandling.mapper;

import java.lang.annotation.*;

/**
 * Marks an implementation of an exception mapper that should be discovered by Katharsis during startup
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionMapperProvider {
}
