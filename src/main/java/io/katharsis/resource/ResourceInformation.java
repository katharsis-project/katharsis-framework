package io.katharsis.resource;

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
    private final ResourceField idField;

    /**
     * A set of resource's attribute fields.
     */
    private final Set<ResourceField> attributeFields;

    /**
     * A set of fields that contains non-standard Java types (List, Set, custom classes, ...).
     */
    private final Set<ResourceField> relationshipFields;

    public ResourceInformation(Class<?> resourceClass, ResourceField idField, Set<ResourceField> attributeFields,
        Set<ResourceField> relationshipFields) {
        this.resourceClass = resourceClass;
        this.idField = idField;
        this.attributeFields = attributeFields;
        this.relationshipFields = relationshipFields;
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public ResourceField getIdField() {
        return idField;
    }

    public Set<ResourceField> getAttributeFields() {
        return attributeFields;
    }

    public Set<ResourceField> getRelationshipFields() {
        return relationshipFields;
    }

    public ResourceField findAttributeFieldByName(String name) {
        return getField(name, attributeFields);
    }

    public ResourceField findRelationshipFieldByName(String name) {
        return getField(name, relationshipFields);
    }

    private ResourceField getField(String name, Set<ResourceField> fields) {
        ResourceField foundField = null;
        for (ResourceField field : fields) {
            if (field.getName().equals(name)) {
                foundField = field;
                break;
            }
        }
        return foundField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ResourceInformation))
            return false;
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