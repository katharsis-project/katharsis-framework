package io.katharsis.resource;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.ResourceFieldException;
import io.katharsis.resource.exception.ResourceIdNotFoundException;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * A builder which creates ResourceInformation instances of a specific class.
 */
public class ResourceInformationBuilder {

    public <T> ResourceInformation<T> build(Class<T> resourceClass) {

        ResourceInformation<T> resourceInformation = new ResourceInformation<>();
        Field idField = getIdField(resourceClass);
        resourceInformation.setIdField(idField);
        resourceInformation.setBasicFields(getBasicFields(resourceClass, idField));
        resourceInformation.setRelationshipFields(getRelationshipFields(resourceClass, idField));

        return resourceInformation;
    }

    private <T> Field getIdField(Class<T> resourceClass) {
        for (Field field : resourceClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonApiId.class)) {
                return field;
            }
        }
        throw new ResourceIdNotFoundException("Id field for class not found: " + resourceClass.getCanonicalName());
    }

    private <T> Set<Field> getBasicFields(Class<T> resourceClass, Field idField) {
        Set<Field> fields = new HashSet<>();
        for (Field field : resourceClass.getDeclaredFields()) {
            if (!isRelationshipType(field) && !field.equals(idField)) {
                fields.add(field);
                if (isRestrictedMember(field.getName())) {
                    throw new ResourceFieldException("Field " + field.getName() + " of class "
                            + resourceClass.getCanonicalName() + "is restricted");
                }
            }
        }
        return fields;
    }

    private <T> Set<Field> getRelationshipFields(Class<T> resourceClass, Field idField) {
        Set<Field> fields = new HashSet<>();
        for (Field field : resourceClass.getDeclaredFields()) {
            if (isRelationshipType(field) && !field.equals(idField)) {
                if (isRestrictedMember(field.getName())) {
                    throw new ResourceFieldException("Field " + field.getName() + " of class "
                            + resourceClass.getCanonicalName() + "is restricted");
                }
                fields.add(field);
            }
        }
        return fields;
    }

    private boolean isRelationshipType(Field type) {
        return type.isAnnotationPresent(JsonApiToMany.class) || type.isAnnotationPresent(JsonApiToOne.class);
    }

    private boolean isRestrictedMember(String fieldName) {
        for (RestrictedMembers c : RestrictedMembers.values()) {
            if (c.name().equals(fieldName)) {
                return true;
            }
        }

        return false;
    }
}
