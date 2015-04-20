package io.katharsis.resource;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

/**
 * Holds information about the type of the resource.
 *
 * @param <T> Type of the resource
 */
public class ResourceInformation<T> {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceInformation<?> that = (ResourceInformation<?>) o;
        return Objects.equals(idField, that.idField) &&
                Objects.equals(basicFields, that.basicFields) &&
                Objects.equals(relationshipFields, that.relationshipFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idField, basicFields, relationshipFields);
    }
}
