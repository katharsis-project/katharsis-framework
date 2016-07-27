package io.katharsis.resource.field;

import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResourceField {
    private final String jsonName;
    private final String underlyingName;
    private final Class<?> type;
    private final Type genericType;
    private List<Annotation> annotations;

    public ResourceField(@SuppressWarnings("SameParameterValue") String jsonName,
                         @SuppressWarnings("SameParameterValue") String underlyingName, Class<?> type, Type genericType) {
        this(jsonName, underlyingName, type, genericType, Collections.<Annotation>emptyList());
    }

    public ResourceField(String jsonName, String underlyingName, Class<?> type, Type genericType, List<Annotation> annotations) {
        this.jsonName = jsonName;
        this.underlyingName = underlyingName;
        this.type = type;
        this.genericType = genericType;
        this.annotations = annotations;
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getUnderlyingName() {
        return underlyingName;
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean isAnnotationPresent(Class<?> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a flag which indicate if a field should not be serialized automatically.
     *
     * @see JsonApiToMany#lazy()
     * @return true if a field is lazy
     */
    public boolean isLazy() {
        JsonApiIncludeByDefault includeByDefaultAnnotation = null;
        JsonApiToMany toManyAnnotation = null;
        JsonApiToOne toOneAnnotation = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(JsonApiIncludeByDefault.class)) {
                includeByDefaultAnnotation = (JsonApiIncludeByDefault) annotation;
            }
            if (annotation.annotationType().equals(JsonApiToMany.class)) {
                toManyAnnotation = (JsonApiToMany) annotation;
            }
            if (annotation.annotationType().equals(JsonApiToOne.class)) {
                toOneAnnotation = (JsonApiToOne) annotation;
            }
        }
        if (includeByDefaultAnnotation != null) {
            return false;
        } else if (toManyAnnotation != null) {
            return toManyAnnotation.lazy();
        } else if (toOneAnnotation != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ResourceField that = (ResourceField) o;
        return Objects.equals(jsonName, that.jsonName) &&
            Objects.equals(underlyingName, that.underlyingName) &&
            Objects.equals(type, that.type) &&
            Objects.equals(genericType, that.genericType) &&
            Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonName, underlyingName, type, genericType, annotations);
    }
}