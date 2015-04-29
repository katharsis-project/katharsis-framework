package io.katharsis.response;

import io.katharsis.jackson.serializer.RelationshipContainerSerializer;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * A class responsible for storing information about a relationship between two resources. The resulting JSON
 * serialized using {@link RelationshipContainerSerializer} is shown below:
 * <pre>
 * {@code
 * {
 *   self: "a link to the resource's linkage",
 *   related: "a link to the resource's filed",
 *   linkage: null
 * }
 * }
 * </pre>
 *
 * @see RelationshipContainerSerializer
 */
public class RelationshipContainer {
    private final DataLinksContainer dataLinksContainer;
    private final Field relationshipField;

    public RelationshipContainer(DataLinksContainer dataLinksContainer, Field relationshipField) {
        this.dataLinksContainer = dataLinksContainer;
        this.relationshipField = relationshipField;
    }

    public Field getRelationshipField() {
        return relationshipField;
    }

    public DataLinksContainer getDataLinksContainer() {
        return dataLinksContainer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipContainer that = (RelationshipContainer) o;
        return Objects.equals(dataLinksContainer, that.dataLinksContainer) &&
                Objects.equals(relationshipField, that.relationshipField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataLinksContainer, relationshipField);
    }
}
