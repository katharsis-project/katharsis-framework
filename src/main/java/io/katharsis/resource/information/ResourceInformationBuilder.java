package io.katharsis.resource.information;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.init.MultipleJsonApiLinksInformationException;
import io.katharsis.resource.exception.init.MultipleJsonApiMetaInformationException;
import io.katharsis.resource.exception.init.ResourceDuplicateIdException;
import io.katharsis.resource.exception.init.ResourceIdNotFoundException;
import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.field.FieldOrderedComparator;
import io.katharsis.resource.information.field.ResourceFieldWrapper;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.java.Optional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

        Optional<JsonPropertyOrder> propertyOrder = ClassUtils.getAnnotation(resourceClass, JsonPropertyOrder.class);
        Set<ResourceField> basicFields = getBasicFields(resourceFields, idField, propertyOrder);
        Set<ResourceField> relationshipFields = getRelationshipFields(resourceFields, idField, propertyOrder);
        ResourceAttributesBridge<?> attributesBridge = new ResourceAttributesBridge(basicFields, resourceClass);

        String metaFieldName = getMetaFieldName(resourceClass, resourceFields);
        String linksFieldName = getLinksFieldName(resourceClass, resourceFields);

        return new ResourceInformation(
            resourceClass,
            idField,
            attributesBridge,
            relationshipFields,
            metaFieldName,
            linksFieldName);
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
            String jsonName = resourceFieldNameTransformer.getName(field);
            String underlyingName = field.getName();
            List<Annotation> annotations = Arrays.asList(field.getAnnotations());
            ResourceField resourceField = new ResourceField(jsonName, underlyingName, field.getType(), field.getGenericType(), annotations);
            if (Modifier.isTransient(field.getModifiers()) ||
                Modifier.isStatic(field.getModifiers())) {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, true));
            } else {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, false));
            }
        }
        return fieldWrappers;
    }

    private List<ResourceFieldWrapper> getGetterResourceFields(List<Method> classGetters) {
        List<ResourceFieldWrapper> fieldWrappers = new ArrayList<>(classGetters.size());
        for (Method getter : classGetters) {
            String jsonName = resourceFieldNameTransformer.getName(getter);
            String underlyingName = resourceFieldNameTransformer.getMethodName(getter);
            List<Annotation> annotations = Arrays.asList(getter.getAnnotations());
            ResourceField resourceField = new ResourceField(jsonName, underlyingName, getter.getReturnType(), getter.getGenericReturnType(), annotations);
            if (Modifier.isStatic(getter.getModifiers())) {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, true));
            } else {
                fieldWrappers.add(new ResourceFieldWrapper(resourceField, false));
            }
        }
        return fieldWrappers;
    }

    private List<ResourceField> getResourceFields(List<ResourceFieldWrapper> resourceClassFields, List<ResourceFieldWrapper> resourceGetterFields) {
        Map<String, ResourceField> resourceFieldMap = new HashMap<>();

        for (ResourceFieldWrapper fieldWrapper : resourceClassFields) {
            if (!fieldWrapper.isDiscarded())
                resourceFieldMap.put(fieldWrapper.getResourceField().getUnderlyingName(), fieldWrapper.getResourceField());
        }

        for (ResourceFieldWrapper fieldWrapper : resourceGetterFields) {
            if (!fieldWrapper.isDiscarded()) {
                String originalName = fieldWrapper.getResourceField().getUnderlyingName();
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

    private static boolean hasDiscardedField(ResourceFieldWrapper fieldWrapper, List<ResourceFieldWrapper> resourceClassFields) {
        for (ResourceFieldWrapper resourceFieldWrapper : resourceClassFields) {
            if (fieldWrapper.getResourceField().getUnderlyingName()
                .equals(resourceFieldWrapper.getResourceField().getUnderlyingName())) {
                return true;
            }
        }
        return false;
    }

    private static ResourceField mergeAnnotations(ResourceField fromField, ResourceField fromMethod) {
        List<Annotation> annotations = new LinkedList<>(fromField.getAnnotations());
        annotations.addAll(fromMethod.getAnnotations());

        return new ResourceField(fromField.getJsonName(), fromField.getUnderlyingName(), fromField.getType(), fromField.getGenericType(), annotations);
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

    private <T> String getMetaFieldName(Class<T> resourceClass, List<ResourceField> classFields) {
        List<ResourceField> metaFields = new ArrayList<>(1);
        for (ResourceField field : classFields) {
            if (field.isAnnotationPresent(JsonApiMetaInformation.class)) {
                metaFields.add(field);
            }
        }

        if (metaFields.size() == 0) {
            return null;
        } else if (metaFields.size() > 1) {
            throw new MultipleJsonApiMetaInformationException(resourceClass.getCanonicalName());
        }
        return metaFields.get(0).getUnderlyingName();
    }

    private <T> String getLinksFieldName(Class<T> resourceClass, List<ResourceField> classFields) {
        List<ResourceField> linksFields = new ArrayList<>(1);
        for (ResourceField field : classFields) {
            if (field.isAnnotationPresent(JsonApiLinksInformation.class)) {
                linksFields.add(field);
            }
        }

        if (linksFields.size() == 0) {
            return null;
        } else if (linksFields.size() > 1) {
            throw new MultipleJsonApiLinksInformationException(resourceClass.getCanonicalName());
        }
        return linksFields.get(0).getUnderlyingName();
    }

    private Set<ResourceField> getBasicFields(List<ResourceField> classFields, ResourceField idField,
                                              Optional<JsonPropertyOrder> propertyOrder) {
        Set<ResourceField> basicFields = buildResourceFieldSet(propertyOrder);
        for (ResourceField field : classFields) {
            if (isBasicField(field) && !field.equals(idField)) {
                basicFields.add(field);
            }
        }

        return basicFields;
    }

    private boolean isBasicField(ResourceField field) {
        return !isRelation(field) &&
            !field.isAnnotationPresent(JsonApiMetaInformation.class) &&
            !field.isAnnotationPresent(JsonApiLinksInformation.class);
    }

    private Set<ResourceField> getRelationshipFields(List<ResourceField> classFields, ResourceField idField,
                                                     Optional<JsonPropertyOrder> propertyOrder) {
        Set<ResourceField> relationshipFields = buildResourceFieldSet(propertyOrder);
        for (ResourceField field : classFields) {
            if (isRelation(field) && !field.equals(idField)) {
                relationshipFields.add(field);
            }
        }

        return relationshipFields;
    }

    private static Set<ResourceField> buildResourceFieldSet(Optional<JsonPropertyOrder> propertyOrderOptional) {
        Set<ResourceField> basicFields;
        if (propertyOrderOptional.isPresent()) {
            JsonPropertyOrder propertyOrder = propertyOrderOptional.get();
            basicFields = new TreeSet<>(new FieldOrderedComparator(propertyOrder.value(), propertyOrder.alphabetic()));
        } else {
            basicFields = new HashSet<>();
        }
        return basicFields;
    }

    private boolean isRelation(ResourceField field) {
        return field.isAnnotationPresent(JsonApiToMany.class) || field.isAnnotationPresent(JsonApiToOne.class);
    }
}
