package io.katharsis.resource.field.attribute;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.utils.ClassUtils;

import java.lang.reflect.Method;
import java.util.Set;

public class ResourceAttributeBuilder {

    public ResourceAttributesBridge build(Class<?> resourceClass, Set<ResourceField> fields) {
        Method method = ClassUtils.findMethodWith(resourceClass, JsonAnyGetter.class);
        if (method == null) {
            return new StaticResourceAttributesBridge(fields);
        } else {
            return new DynamicResourceAttributesBridge(method);
        }
    }
}
