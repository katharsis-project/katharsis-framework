package io.katharsis.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResourceField {
    private String name;
    private Class<?> type;
    private Type genericType;
    private List<Annotation> annotations = Collections.emptyList();

    public ResourceField(String name, Class<?> type, Type genericType) {
        this.name = name;
        this.type = type;
        this.genericType = genericType;
    }

    public ResourceField(String name, Class<?> type, Type genericType, List<Annotation> annotations) {
        this.name = name;
        this.type = type;
        this.genericType = genericType;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
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
        return annotations.stream()
            .filter(annotation -> annotation.annotationType().equals(annotationClass))
            .findAny()
            .isPresent();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ResourceField that = (ResourceField) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(type, that.type) &&
            Objects.equals(genericType, that.genericType) &&
            Objects.equals(annotations, that.annotations);
    }

    @Override public int hashCode() {
        return Objects.hash(name, type, genericType, annotations);
    }
}