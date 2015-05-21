package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;

/**
 * Returns a name of a field. It takes into account {@link JsonProperty} annotation.
 */
public class ResourceFieldNameTransformer {

    public String getName(Field field) {
        String name = field.getName();
        if (field.isAnnotationPresent(JsonProperty.class) &&
                !"".equals(field.getAnnotation(JsonProperty.class).value())) {
            name = field.getAnnotation(JsonProperty.class).value();
        }
        return name;
    }
}
