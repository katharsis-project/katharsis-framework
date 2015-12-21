package io.katharsis.resource.information;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.init.ResourceDuplicateIdException;
import io.katharsis.resource.exception.init.ResourceIdNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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

        List<ResourceFieldWrapper> resourceClassFields = getFieldResourceFields(classFields);
        List<ResourceFieldWrapper> resourceGetterFields = getGetterResourceFields(classGetters);
        return getResourceFields(resourceClassFields, resourceGetterFields);
    }

    private List<ResourceFieldWrapper> getFieldResourceFields(List<Field> classFields) {
        List<ResourceFieldWrapper> fieldWrappers = new ArrayList<>(classFields.size());
        for (Field field : classFields) {
            String name = resourceFieldNameTransformer.getName(field);
            List<Annotation> annotations = Arrays.asList(field.getAnnotations());
            ResourceField resourceField = new ResourceField(name, field.getType(), field.getGenericType(), annotations);
            if (Modifier.isTransient(field.getModifiers()) ||
                Modifier.isStatic(field.getModifiers())) {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, true, field.getName()));
            } else {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, false, field.getName()));
            }
        }
        return fieldWrappers;
    }

    private List<ResourceFieldWrapper> getGetterResourceFields(List<Method> classGetters) {
        List<ResourceFieldWrapper> fieldWrappers = new ArrayList<>(classGetters.size());
        for (Method getter : classGetters) {
            String name = resourceFieldNameTransformer.getName(getter);
            String originalName = resourceFieldNameTransformer.getMethodName(getter);
            List<Annotation> annotations = Arrays.asList(getter.getAnnotations());
            ResourceField resourceField = new ResourceField(name, getter.getReturnType(), getter.getGenericReturnType(), annotations);
            if (Modifier.isStatic(getter.getModifiers())) {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, true, originalName));
            } else {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, false, originalName));
            }
        }
        return fieldWrappers;
    }

    private List<ResourceField> getResourceFields(List<ResourceFieldWrapper> resourceClassFields, List<ResourceFieldWrapper> resourceGetterFields) {
        Map<String, ResourceField> resourceFieldMap = new HashMap<>();

        for (ResourceFieldWrapper fieldWrapper : resourceClassFields) {
            if (!fieldWrapper.isDiscarded())
                resourceFieldMap.put(fieldWrapper.getOriginalName(), fieldWrapper.getResourceField());
        }

        for (ResourceFieldWrapper fieldWrapper : resourceGetterFields) {
            if (!fieldWrapper.isDiscarded()) {
                String originalName = fieldWrapper.getOriginalName();
                ResourceField field = fieldWrapper.getResourceField();
                if (resourceFieldMap.containsKey(originalName)) {
                    resourceFieldMap.put(originalName, mergeAnnotations(resourceFieldMap.get(originalName), field));
                } else if (!hasDiscardedField(fieldWrapper, resourceClassFields)) {
                    resourceFieldMap.put(originalName, field);
                }
            }
        }

        return discardIgnoredField(resourceFieldMap.values());
    }

    private List<ResourceField> discardIgnoredField(Collection<ResourceField> resourceFieldValues) {
        List<ResourceField> resourceFields = new LinkedList<>();
        for (ResourceField resourceField : resourceFieldValues) {
            if (!resourceField.isAnnotationPresent(JsonIgnore.class)) {
                resourceFields.add(resourceField);
            }
        }

        return resourceFields;
    }

    private boolean hasDiscardedField(ResourceFieldWrapper fieldWrapper, List<ResourceFieldWrapper> resourceClassFields) {
        for (ResourceFieldWrapper resourceFieldWrapper : resourceClassFields) {
            if (fieldWrapper.getOriginalName().equals(resourceFieldWrapper.getOriginalName())) {
                return true;
            }
        }
        return false;
    }

    private ResourceField mergeAnnotations(ResourceField fromField, ResourceField fromMethod) {
        List<Annotation> annotations = new LinkedList<>(fromField.getAnnotations());
        annotations.addAll(fromMethod.getAnnotations());

        return new ResourceField(fromField.getName(), fromField.getType(), fromField.getGenericType(), annotations);
    }

    private <T> ResourceField getIdField(Class<T> resourceClass, List<ResourceField> classFields) {
        List<ResourceField> idFields = new ArrayList<>(1);
        for (ResourceField field : classFields) {
            if (field.isAnnotationPresent(JsonApiId.class)) {
                idFields.add(field);
            }
        }

        if (idFields.size() == 0) {
            throw new ResourceIdNotFoundException(resourceClass.getCanonicalName());
        } else if (idFields.size() > 1) {
            throw new ResourceDuplicateIdException(resourceClass.getCanonicalName());
        }
        return idFields.get(0);
    }

    private Set<ResourceField> getBasicFields(List<ResourceField> classFields, ResourceField idField) {
        Set<ResourceField> basicFields = new HashSet<>();
        for (ResourceField field : classFields) {
            if (!isRelation(field) && !field.equals(idField)) {
                basicFields.add(field);
            }
        }

        return basicFields;
    }

    private Set<ResourceField> getRelationshipFields(List<ResourceField> classFields, ResourceField idField) {
        Set<ResourceField> relationshipFields = new HashSet<>();
        for (ResourceField field : classFields) {
            if (isRelation(field) && !field.equals(idField)) {
                relationshipFields.add(field);
            }
        }

        return relationshipFields;
    }

    private boolean isRelation(ResourceField field) {
        return field.isAnnotationPresent(JsonApiToMany.class) || field.isAnnotationPresent(JsonApiToOne.class);
    }
}
