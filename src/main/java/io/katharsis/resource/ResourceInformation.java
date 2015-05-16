package io.katharsis.resource;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

/**
 * Holds information about the type of the resource.
 */
public final class ResourceInformation {

    private static final ResourceFieldNameTransformer RESOURCE_FIELD_NAME_TRANSFORMER = new ResourceFieldNameTransformer();

    private final Class<?> resourceClass;

    /**
     * Found field of the id. Each resource has to contain a field marked by JsonApiId annotation.
     */
    private final Field idField;

    /**
     * A set of resource's attribute fields.
     */
    private final Set<Field> attributeFields;

    /**
     * A set of fields that contains non-standard Java types (List, Set, custom classes, ...).
     */
    private final Set<Field> relationshipFields;

    public ResourceInformation(Class<?> resourceClass, Field idField, Set<Field> attributeFields, Set<Field> relationshipFields) {
        this.resourceClass = resourceClass;
        this.idField = idField;
        this.attributeFields = attributeFields;
        this.relationshipFields = relationshipFields;
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public Field getIdField() {
        return idField;
    }


    public Set<Field> getAttributeFields() {
        return attributeFields;
    }

    public Set<Field> getRelationshipFields() {
        return relationshipFields;
    }

    public Field findAttributeFieldByName(String name) {
        return getField(name, attributeFields);
    }

    public Field findRelationshipFieldByName(String name) {
        return getField(name, relationshipFields);
    }

    private Field getField(String name, Set<Field> fields) {
        Field foundField = null;
        String fieldName;
        for (Field field : fields) {
            fieldName = RESOURCE_FIELD_NAME_TRANSFORMER.getName(field);
            if (fieldName.equals(name)) {
                foundField = field;
                break;
            }
        }
        return foundField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceInformation)) return false;
        ResourceInformation that = (ResourceInformation) o;
        return Objects.equals(resourceClass, that.resourceClass) &&
                Objects.equals(idField, that.idField) &&
                Objects.equals(attributeFields, that.attributeFields) &&
                Objects.equals(relationshipFields, that.relationshipFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceClass, idField, attributeFields, relationshipFields);
    }
}