package io.katharsis.resource.field;

import io.katharsis.resource.annotations.JsonApiToMany;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ResourceField {
    private final String name;
    private final Class<?> type;
    private final Type genericType;
    private List<Annotation> annotations;

    public ResourceField(@SuppressWarnings("SameParameterValue") String name, Class<?> type, Type genericType) {
        this(name, type, genericType, Collections.emptyList());
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

    public boolean isLazy() {
        boolean isLazy = false;
        Optional<JsonApiToMany> toManyOptional = annotations.stream()
            .filter(annotation -> annotation.annotationType().equals(JsonApiToMany.class))
            .map(annotation -> (JsonApiToMany) annotation)
            .findAny();
        if (toManyOptional.isPresent()) {
            isLazy = toManyOptional.get().lazy();
        }
        return isLazy;
    }

    @Override
    public boolean equals(Object o) {
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

    @Override
    public int hashCode() {
        return Objects.hash(name, type, genericType, annotations);
    }
}