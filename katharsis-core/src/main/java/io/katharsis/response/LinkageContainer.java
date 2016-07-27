package io.katharsis.response;

import io.katharsis.jackson.serializer.LinkageContainerSerializer;
import io.katharsis.resource.registry.RegistryEntry;

import java.util.Objects;

/**
 * A class responsible for storing information about the linkage information. The resulting JSON serialized using
 * {@link LinkageContainerSerializer} is show below:
 * <pre>
 * {@code
 * {
 *   type: "type of the resource",
 *   id: "id of the resource"
 * }
 * }
 * </pre>
 *
 * @see LinkageContainerSerializer
 */
public class LinkageContainer {

    private final Object objectItem;
    private final Class relationshipClass;
    private final RegistryEntry relationshipEntry;

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
