package io.katharsis.resource.field.attribute;

import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.field.ResourceField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Uses a method which is annotated with {@link com.fasterxml.jackson.annotation.JsonAnySetter} to retrieve a set of
 * attributes
 */
public class DynamicResourceAttributesBridge implements ResourceAttributesBridge {

    private final Method jsonAnyGetter;
    private final Method jsonAnyGetter;

    public DynamicResourceAttributesBridge(Method jsonAnyGetter, Method jsonAnySetter) {
        this.jsonAnyGetter = jsonAnyGetter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<ResourceField> getAttributes(Object resource) {
        Map<String, Object> attributes;
        try {
            attributes = (Map<String, Object>) jsonAnyGetter.invoke(resource);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ResourceException(String.format("Error while accessing resource properties: %s", e.getMessage()));
        }

        Set<ResourceField> resourceFields = new HashSet<>(attributes.size());

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            String name = attribute.getKey();
            Class<?> valueType = attribute.getValue() != null ? attribute.getValue().getClass() : Void.class;
            resourceFields.add(new ResourceField(name, name, valueType, valueType));
        }
        return resourceFields;
    }

    @Override
    public void setProperty(Object instance, String propertyName, Object property) {

    }
}
