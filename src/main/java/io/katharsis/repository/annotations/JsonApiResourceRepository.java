package io.katharsis.repository.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiResourceRepository {
    Class<?> value();
}
