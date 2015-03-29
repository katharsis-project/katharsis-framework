package io.katharsis.resource;

import java.lang.reflect.Field;
import java.util.HashSet;
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

    /**
     * Returns resource's all fields.
     *
     * @return All fields of a resource
     */
    public Set<Field> getAllFields() {
        HashSet<Field> fields = new HashSet<>(basicFields);
        fields.addAll(relationshipFields);
        fields.add(idField);

        return fields;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceInformation that = (ResourceInformation) o;

        if (basicFields != null ? !basicFields.equals(that.basicFields) : that.basicFields != null) return false;
        if (idField != null ? !idField.equals(that.idField) : that.idField != null) return false;
        if (relationshipFields != null ? !relationshipFields.equals(that.relationshipFields) : that.relationshipFields != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = idField != null ? idField.hashCode() : 0;
        result = 31 * result + (basicFields != null ? basicFields.hashCode() : 0);
        result = 31 * result + (relationshipFields != null ? relationshipFields.hashCode() : 0);
        return result;
    }
}
