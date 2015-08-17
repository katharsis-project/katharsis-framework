package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.init.ResourceDuplicateIdException;
import io.katharsis.resource.exception.init.ResourceIdNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A builder which creates ResourceInformation instances of a specific class. It extracts information about a resource
 * from annotations.
 */
public final class ResourceInformationBuilder {

    private final ResourceFieldNameTransformer resourceFieldNameTransformer;

    public ResourceInformationBuilder(ResourceFieldNameTransformer resourceFieldNameTransformer) {
        this.resourceFieldNameTransformer = resourceFieldNameTransformer;
    }

    public ResourceInformation build(Class<?> resourceClass) {
        ResourceField idField = getIdField(resourceClass);
        return new ResourceInformation(
            resourceClass,
            idField,
            getBasicFields(resourceClass, idField),
            getRelationshipFields(resourceClass, idField));
    }

    private <T> ResourceField getIdField(Class<T> resourceClass) {
        List<Field> idFields = Arrays.stream(resourceClass.getDeclaredFields())
            .filter(this::isIdField)
            .collect(Collectors.toList());

        if (idFields.size() == 0) {
            throw new ResourceIdNotFoundException(resourceClass.getCanonicalName());
        } else if (idFields.size() > 1) {
            throw new ResourceDuplicateIdException(resourceClass.getCanonicalName());
        }
        Field javaField = idFields.get(0);
        String fieldName = resourceFieldNameTransformer.getName(javaField);
        List<Annotation> annotations = Arrays.asList(javaField.getAnnotations());
        return new ResourceField(fieldName, javaField.getType(), javaField.getGenericType(), annotations);
    }

    private boolean isIdField(Field field) {
        return field.isAnnotationPresent(JsonApiId.class) && !isIgnorable(field);
    }

    private boolean isIgnorable(Field field) {
        return field.isAnnotationPresent(JsonIgnore.class) || Modifier.isTransient(field.getModifiers()) || field
            .isSynthetic();
    }

    private <T> Set<ResourceField> getBasicFields(Class<T> resourceClass, ResourceField idField) {
        return Arrays.stream(resourceClass.getDeclaredFields())
            .filter(field -> !isRelationshipType(field) && !isIgnorable(field))
            .map(field -> {
                String name = resourceFieldNameTransformer.getName(field);
                List<Annotation> annotations = Arrays.asList(field.getAnnotations());
                return new ResourceField(name, field.getType(), field.getGenericType(), annotations);
            })
            .filter(field -> !field.equals(idField))
            .collect(Collectors.toSet());
    }

    private <T> Set<ResourceField> getRelationshipFields(Class<T> resourceClass, ResourceField idField) {
        return Arrays.stream(resourceClass.getDeclaredFields())
            .filter(this::isRelationshipType)
            .map(field -> {
                String name = resourceFieldNameTransformer.getName(field);
                List<Annotation> annotations = Arrays.asList(field.getAnnotations());
                return new ResourceField(name, field.getType(), field.getGenericType(), annotations);
            })
            .filter(field -> !field.equals(idField))
            .collect(Collectors.toSet());
    }

    private boolean isRelationshipType(Field type) {
        return type.isAnnotationPresent(JsonApiToMany.class) || type.isAnnotationPresent(JsonApiToOne.class);
    }
}
