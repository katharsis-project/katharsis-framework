package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.init.ResourceDuplicateIdException;
import io.katharsis.resource.exception.init.ResourceIdNotFoundException;
import io.katharsis.utils.PropertyUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A builder which creates ResourceInformation instances of a specific class. It extracts information about a resource
 * from annotations and information about fields.
 */
public final class ResourceInformationBuilder {

    private final ResourceFieldNameTransformer resourceFieldNameTransformer;

    public ResourceInformationBuilder(ResourceFieldNameTransformer resourceFieldNameTransformer) {
        this.resourceFieldNameTransformer = resourceFieldNameTransformer;
    }

    public ResourceInformation build(Class<?> resourceClass) {
        List<Field> classFields = PropertyUtils.getClassFields(resourceClass);

        ResourceField idField = getIdField(resourceClass, classFields);
        Set<ResourceField> basicFields = getBasicFields(classFields, idField);
        Set<ResourceField> relationshipFields = getRelationshipFields(classFields, idField);

        return new ResourceInformation(
            resourceClass,
            idField,
            basicFields,
            relationshipFields);
    }

    private <T> ResourceField getIdField(Class<T> resourceClass, List<Field> classFields) {
        List<Field> idFields = classFields.stream()
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

    private <T> Set<ResourceField> getBasicFields(List<Field> classFields, ResourceField idField) {
        return classFields.stream()
            .filter(field -> !isRelationshipType(field) && !isIgnorable(field))
            .map(field -> {
                String name = resourceFieldNameTransformer.getName(field);
                List<Annotation> annotations = Arrays.asList(field.getAnnotations());
                return new ResourceField(name, field.getType(), field.getGenericType(), annotations);
            })
            .filter(field -> !field.equals(idField))
            .collect(Collectors.toSet());
    }

    private <T> Set<ResourceField> getRelationshipFields(List<Field> classFields, ResourceField idField) {
        return classFields.stream()
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
