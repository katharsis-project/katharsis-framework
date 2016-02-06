package io.katharsis.response;

import io.katharsis.jackson.serializer.DataLinksContainerSerializer;
import io.katharsis.resource.field.ResourceField;

import java.util.Objects;
import java.util.Set;

/**
 * A class responsible for storing information about links within resource object, that is link to the resource itself
 * and relationships. The resulting JSON serialized using {@link DataLinksContainerSerializer} is
 * shown below:
 * <pre>
 * {@code
 * {
 *   self: "url to the resource"
 * }
 * }
 * </pre>
 *
 * @see DataLinksContainerSerializer
 */
public class DataLinksContainer {
    private final Object data;
    private final Set<ResourceField> relationshipFields;

    public DataLinksContainer(Object data, Set<ResourceField> relationshipFields) {
        this.data = data;
        this.relationshipFields = relationshipFields;
    }

    public Object getData() {
        return data;
    }

    public Set<ResourceField> getRelationshipFields() {
        return relationshipFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataLinksContainer that = (DataLinksContainer) o;
        return Objects.equals(data, that.data) &&
                Objects.equals(relationshipFields, that.relationshipFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, relationshipFields);
    }
}
