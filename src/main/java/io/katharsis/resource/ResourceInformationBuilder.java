package io.katharsis.resource;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceIdNotFoundException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A builder which creates ResourceInformation instances of a specific class. It extracts information about a resource
 * from annotations.
 */
public final class ResourceInformationBuilder {

    public ResourceInformation build(Class<?> resourceClass) {
        Field idField = getIdField(resourceClass);
        return new ResourceInformation(
                resourceClass,
                idField,
                getBasicFields(resourceClass, idField),
                getRelationshipFields(resourceClass, idField));
    }

    private <T> Field getIdField(Class<T> resourceClass) {
        List<Field> idFields = Arrays.stream(resourceClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JsonApiId.class))
                .collect(Collectors.toList());

        if (idFields.size() == 0) {
            throw new ResourceIdNotFoundException("Id field for class not found: " + resourceClass.getCanonicalName());
        } else if (idFields.size() > 1) {
            throw new ResourceException("Duplicated Id field found in class: " + resourceClass.getCanonicalName());
        }
        return idFields.get(0);
    }

    private <T> Set<Field> getBasicFields(Class<T> resourceClass, Field idField) {
        return Arrays.stream(resourceClass.getDeclaredFields())
                .filter(field -> !isRelationshipType(field) && !field.equals(idField) && !field.isSynthetic())
                .collect(Collectors.toSet());
    }

    private <T> Set<Field> getRelationshipFields(Class<T> resourceClass, Field idField) {
        return Arrays.stream(resourceClass.getDeclaredFields())
                .filter(field -> isRelationshipType(field) && !field.equals(idField))
                .collect(Collectors.toSet());
    }

    private boolean isRelationshipType(Field type) {
        return type.isAnnotationPresent(JsonApiToMany.class) || type.isAnnotationPresent(JsonApiToOne.class);
    }
}
