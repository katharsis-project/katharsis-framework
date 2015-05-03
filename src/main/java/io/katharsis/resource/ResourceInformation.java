package io.katharsis.resource;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

/**
 * Holds information about the type of the resource.
 */
public class ResourceInformation {

    private Class<?> resourceClass;

    /**
     * Found field of the id. Each resource has to contain a field marked by JsonApiId annotation.
     */
    private Field idField;

    /**
     * A set of fields that has basic Java types (String, Long, ...).
     */
    private Set<Field> basicFields;

    /**
     * A set of fields that contains non-standard Java types (List, Set, custom classes, ...).
     */
    private Set<Field> relationshipFields;

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Field getIdField() {
        return idField;
    }

    public void setIdField(Field idField) {
        this.idField = idField;
    }

    public Set<Field> getBasicFields() {
        return basicFields;
    }

    public void setBasicFields(Set<Field> basicFields) {
        this.basicFields = basicFields;
    }

    public Set<Field> getRelationshipFields() {
        return relationshipFields;
    }

    public void setRelationshipFields(Set<Field> relationshipFields) {
        this.relationshipFields = relationshipFields;
    }

    public Field findRelationshipFieldByName(String name) {
        Field foundField = null;
        if (relationshipFields != null) {
            for (Field field : relationshipFields) {
                if (field.getName().equals(name)) {
                    foundField = field;
                    break;
                }
            }
        }
        return foundField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceInformation)) return false;
        ResourceInformation that = (ResourceInformation) o;
        return Objects.equals(resourceClass, that.resourceClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceClass);
    }
}
