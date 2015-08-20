package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.init.ResourceDuplicateIdException;
import io.katharsis.resource.exception.init.ResourceIdNotFoundException;
import io.katharsis.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A builder which creates ResourceInformation instances of a specific class. It extracts information about a resource
 * from annotations and information about fields and getters.
 */
public final class ResourceInformationBuilder {

    private final ResourceFieldNameTransformer resourceFieldNameTransformer;

    public ResourceInformationBuilder(ResourceFieldNameTransformer resourceFieldNameTransformer) {
        this.resourceFieldNameTransformer = resourceFieldNameTransformer;
    }

    public ResourceInformation build(Class<?> resourceClass) {
        List<ResourceField> resourceFields = getResourceFields(resourceClass);


        ResourceField idField = getIdField(resourceClass, resourceFields);
        Set<ResourceField> basicFields = getBasicFields(resourceFields, idField);
        Set<ResourceField> relationshipFields = getRelationshipFields(resourceFields, idField);

        return new ResourceInformation(
            resourceClass,
            idField,
            basicFields,
            relationshipFields);
    }

    private List<ResourceField> getResourceFields(Class<?> resourceClass) {
        List<Field> classFields = ClassUtils.getClassFields(resourceClass);
        List<Method> classGetters = ClassUtils.getClassGetters(resourceClass);

        List<ResourceField> resourceClassFields = getFieldResourceFields(classFields);
        List<ResourceField> resourceGetterFields = getGetterResourceFields(classGetters);
        return getResourceFields(resourceClassFields, resourceGetterFields);
    }

    private List<ResourceField> getFieldResourceFields(List<Field> classFields) {
        return classFields
            .stream()
            .filter(field -> !isIgnorable(field))
            .map(field -> {
                String name = resourceFieldNameTransformer.getName(field);
                List<Annotation> annotations = Arrays.asList(field.getAnnotations());
                return new ResourceField(name, field.getType(), field.getGenericType(), annotations);
            })
            .collect(Collectors.toList());
    }

    private List<ResourceField> getGetterResourceFields(List<Method> classGetters) {
        return classGetters
            .stream()
            .filter(method -> !method.isAnnotationPresent(JsonIgnore.class))
            .map(getter -> {
                String name = resourceFieldNameTransformer.getName(getter);
                List<Annotation> annotations = Arrays.asList(getter.getAnnotations());
                return new ResourceField(name, getter.getReturnType(), getter.getGenericReturnType(), annotations);
            })
            .collect(Collectors.toList());
    }

    private List<ResourceField> getResourceFields(List<ResourceField> resourceClassFields, List<ResourceField> resourceGetterFields) {
        Map<String, ResourceField> resourceFieldMap = new HashMap<>();

        for (ResourceField field : resourceClassFields) {
            resourceFieldMap.put(field.getName(), field);
        }

        for (ResourceField field : resourceGetterFields) {
            resourceFieldMap.putIfAbsent(field.getName(), field);
        }

        return new LinkedList<>(resourceFieldMap.values());

    }

    private <T> ResourceField getIdField(Class<T> resourceClass, List<ResourceField> classFields) {
        List<ResourceField> idFields = classFields.stream()
            .filter(field -> field.isAnnotationPresent(JsonApiId.class))
            .collect(Collectors.toList());

        if (idFields.size() == 0) {
            throw new ResourceIdNotFoundException(resourceClass.getCanonicalName());
        } else if (idFields.size() > 1) {
            throw new ResourceDuplicateIdException(resourceClass.getCanonicalName());
        }
        return idFields.get(0);
    }

    private boolean isIgnorable(Field field) {
        return field.isAnnotationPresent(JsonIgnore.class)
            || Modifier.isTransient(field.getModifiers())
            || field.isSynthetic();
    }

    private Set<ResourceField> getBasicFields(List<ResourceField> classFields, ResourceField idField) {
        return classFields.stream()
            .filter(field -> !field.isAnnotationPresent(JsonApiToMany.class) && !field.isAnnotationPresent
                (JsonApiToOne.class)) // get rid of relations
            .filter(field -> !field.equals(idField))
            .collect(Collectors.toSet());
    }

    private Set<ResourceField> getRelationshipFields(List<ResourceField> classFields, ResourceField idField) {
        return classFields.stream()
            .filter(field -> field.isAnnotationPresent(JsonApiToMany.class)
                || field.isAnnotationPresent(JsonApiToOne.class)) // get only relations
            .filter(field -> !field.equals(idField))
            .collect(Collectors.toSet());
    }
}
