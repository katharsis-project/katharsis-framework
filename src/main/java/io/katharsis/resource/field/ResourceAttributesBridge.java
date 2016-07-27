package io.katharsis.resource.field;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.init.InvalidResourceException;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.java.Optional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Classes which implement those interface are able to provide a set of resource attributes
 */
public class ResourceAttributesBridge<T> {

    private final Set<ResourceField> staticFields;
    private final Class<T> resourceClass;
    private Method jsonAnyGetter;
    private Method jsonAnySetter;

    public ResourceAttributesBridge(Set<ResourceField> staticFields, Class<T> resourceClass) {
        this.staticFields = staticFields;
        this.resourceClass = resourceClass;

        initializeGetterAndSetter(resourceClass);
    }

    private void initializeGetterAndSetter(Class<T> resourceClass) {
        this.jsonAnyGetter = ClassUtils.findMethodWith(resourceClass, JsonAnyGetter.class);
        this.jsonAnySetter = ClassUtils.findMethodWith(resourceClass, JsonAnySetter.class);

        if (absentAnySetter()) {
            throw new InvalidResourceException(
                String.format("A resource %s has to have both methods annotated with @JsonAnySetter and @JsonAnyGetter",
                    resourceClass.getCanonicalName()));
        }
    }

    /**
     * The resource has to have both method annotated with {@link JsonAnySetter} and {@link JsonAnyGetter} to allow
     * proper handling.
     *
     * @return <i>true</i> if resource definition is incomplete, <i>false</i> otherwise
     */
    private boolean absentAnySetter() {
        return (jsonAnySetter == null && jsonAnyGetter != null) ||
            (jsonAnySetter != null && jsonAnyGetter == null);
    }

    /**
     * Sets instance properties using found attributes and {@link JsonAnySetter} annotated method
     *
     * @param objectMapper used to map new attributes
     * @param instance     instance to fill in attributes
     * @param attributes   set od attributes
     */
    public void setProperties(ObjectMapper objectMapper, T instance, JsonNode attributes) {
        T instanceWithNewFields;
        try {
            instanceWithNewFields = objectMapper.readerFor(resourceClass).readValue(attributes);
        } catch (IOException e) {
            throw new ResourceException(
                String.format("Exception while reading %s: %s", instance.getClass(), e.getMessage()));
        }
        Iterator<String> propertyNameIterator = attributes.fieldNames();
        while (propertyNameIterator.hasNext()) {
            setProperty(instance, instanceWithNewFields, propertyNameIterator);
        }

        setAnyProperties(instance, instanceWithNewFields);
    }

    /**
     * Jackson {@link ObjectMapper#readerForUpdating(Object)} cannot be used here, because there might be a case where
     * <i>instance</i> parameter a proxied object e.g. by Hibernate.
     *
     * @param instance              instance of a resource
     * @param instanceWithNewFields a temporary instance with fields to be set
     * @param propertyNameIterator  set of properties
     */
    private void setProperty(T instance, T instanceWithNewFields, Iterator<String> propertyNameIterator) {
        String propertyName = propertyNameIterator.next();
        Optional<ResourceField> staticField = findStaticField(propertyName);
        if (staticField.isPresent()) {
            String underlyingName = staticField.get().getUnderlyingName();
            Object property = PropertyUtils.getProperty(instanceWithNewFields, underlyingName);
            PropertyUtils.setProperty(instance, underlyingName, property);
        } else {
            // Needed for JsonIgnore and dynamic attributes
        }
    }

    /**
     * Get a map of additional attributes and pass to {@link JsonAnySetter} annotated method
     *
     * @param instance              instance to fill in attributes
     * @param instanceWithNewFields temporary instance with new fields
     */
    private void setAnyProperties(T instance, T instanceWithNewFields) {
        if (jsonAnySetter != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> additionalAttributes;
            try {
                additionalAttributes = (Map<String, Object>) jsonAnyGetter.invoke(instanceWithNewFields);
                for (Map.Entry<String, Object> property : additionalAttributes.entrySet()) {
                    jsonAnySetter.invoke(instance, property.getKey(), property.getValue());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ResourceException(
                    String.format("Exception while setting %s: %s", instance.getClass(), e.getMessage()));
            }
        }
    }

    private Optional<ResourceField> findStaticField(String propertyName) {
        for (ResourceField resourceField : staticFields) {
            if (resourceField.getJsonName().equals(propertyName)) {
                return Optional.of(resourceField);
            }
        }
        return Optional.empty();
    }
}
