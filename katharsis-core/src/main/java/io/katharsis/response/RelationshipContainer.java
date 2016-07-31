package io.katharsis.response;

import io.katharsis.jackson.serializer.RelationshipContainerSerializer;
import io.katharsis.resource.field.ResourceField;
import lombok.AllArgsConstructor;
import lombok.Data;

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
@Data
@AllArgsConstructor
public class RelationshipContainer {

    private final DataLinksContainer dataLinksContainer;
    private final ResourceField relationshipField;
    private final boolean forceInclusion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipContainer that = (RelationshipContainer) o;
        return forceInclusion == that.forceInclusion &&
                Objects.equals(dataLinksContainer, that.dataLinksContainer) &&
                Objects.equals(relationshipField, that.relationshipField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataLinksContainer, relationshipField, forceInclusion);
    }
}
