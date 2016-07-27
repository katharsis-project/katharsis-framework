package io.katharsis.resource.information;

import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Holds information about the type of the resource.
 */
public final class ResourceInformation {
    private final Class<?> resourceClass;

    /**
     * Found field of the id. Each resource has to contain a field marked by JsonApiId annotation.
     */
    private final ResourceField idField;

    /**
     * A set of resource's attribute fields.
     */
    private final ResourceAttributesBridge attributeFields;

    /**
     * A set of fields that contains non-standard Java types (List, Set, custom classes, ...).
     */
    private final Set<ResourceField> relationshipFields;


    /**
     * An underlying field's name which contains meta information about for a resource
     */
    private final String metaFieldName;

    /**
     * An underlying field's name which contain links information about for a resource
     */
    private final String linksFieldName;

    public ResourceInformation(Class<?> resourceClass, ResourceField idField, ResourceAttributesBridge attributeFields,
                               Set<ResourceField> relationshipFields) {
        this(resourceClass, idField, attributeFields, relationshipFields, null, null);
    }

    public ResourceInformation(Class<?> resourceClass, ResourceField idField, ResourceAttributesBridge attributeFields,
                               Set<ResourceField> relationshipFields, String metaFieldName, String linksFieldName) {
        this.resourceClass = resourceClass;
        this.idField = idField;
        this.attributeFields = attributeFields;
        this.relationshipFields = relationshipFields;
        this.metaFieldName = metaFieldName;
        this.linksFieldName = linksFieldName;
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public ResourceField getIdField() {
        return idField;
    }

    public ResourceAttributesBridge getAttributeFields() {
        return attributeFields;
    }

    public Set<ResourceField> getRelationshipFields() {
        return relationshipFields;
    }

    public ResourceField findRelationshipFieldByName(String name) {
        return getJsonField(name, relationshipFields);
    }

    private static ResourceField getJsonField(String name, Set<ResourceField> fields) {
        ResourceField foundField = null;
        for (ResourceField field : fields) {
            if (field.getJsonName().equals(name)) {
                foundField = field;
                break;
            }
        }
        return foundField;
    }

    public String getMetaFieldName() {
        return metaFieldName;
    }

    public String getLinksFieldName() {
        return linksFieldName;
    }

    /**
     * Returns a set of field names which are not basic fields (resource attributes)
     *
     * @return not basic attribute names
     */
    public Set<String> getNotAttributeFields() {
        Set<String> notAttributeFields = new HashSet<>();
        for (ResourceField relationshipField : relationshipFields) {
            notAttributeFields.add(relationshipField.getJsonName());
        }
        notAttributeFields.add(idField.getJsonName());
        if (metaFieldName != null) {
            notAttributeFields.add(metaFieldName);
        }
        if (linksFieldName != null) {
            notAttributeFields.add(linksFieldName);
        }
        return notAttributeFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceInformation that = (ResourceInformation) o;
        return Objects.equals(resourceClass, that.resourceClass) &&
            Objects.equals(idField, that.idField) &&
            Objects.equals(attributeFields, that.attributeFields) &&
            Objects.equals(relationshipFields, that.relationshipFields) &&
            Objects.equals(metaFieldName, that.metaFieldName) &&
            Objects.equals(linksFieldName, that.linksFieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceClass, idField, attributeFields, relationshipFields, metaFieldName, linksFieldName);
    }
}