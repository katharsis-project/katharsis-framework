package io.katharsis.resource.annotations;

import java.lang.annotation.*;

/**
 * Created by zachncst on 10/16/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiLookupIncludeAutomatically {
}
