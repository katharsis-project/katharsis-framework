package io.katharsis.response;

import io.katharsis.resource.registry.RegistryEntry;

import java.util.Objects;

public class LinkageContainer {

    private Object objectItem;
    private Class relationshipClass;
    private RegistryEntry relationshipEntry;

    public LinkageContainer(Object objectItem, Class relationshipClass, RegistryEntry relationshipEntry) {
        this.objectItem = objectItem;
        this.relationshipClass = relationshipClass;
        this.relationshipEntry = relationshipEntry;
    }

    public Object getObjectItem() {
        return objectItem;
    }

    public Class getRelationshipClass() {
        return relationshipClass;
    }

    public RegistryEntry getRelationshipEntry() {
        return relationshipEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkageContainer that = (LinkageContainer) o;
        return Objects.equals(objectItem, that.objectItem) &&
                Objects.equals(relationshipClass, that.relationshipClass) &&
                Objects.equals(relationshipEntry, that.relationshipEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectItem, relationshipClass, relationshipEntry);
    }
}
