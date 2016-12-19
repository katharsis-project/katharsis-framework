package io.katharsis.client.internal.core;

import io.katharsis.queryParams.params.IncludedRelationsParams;
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
    private final IncludedRelationsParams includedRelations;
    private final Container container;

    public DataLinksContainer(Object data, Set<ResourceField> relationshipFields, IncludedRelationsParams includedRelations, Container container) {
        this.data = data;
        this.relationshipFields = relationshipFields;
        this.includedRelations = includedRelations;
        this.container = container;
    }

    public Object getData() {
        return data;
    }

    public Set<ResourceField> getRelationshipFields() {
        return relationshipFields;
    }

    public IncludedRelationsParams getIncludedRelations() {
        return includedRelations;
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataLinksContainer that = (DataLinksContainer) o;
        return Objects.equals(data, that.data) &&
                Objects.equals(relationshipFields, that.relationshipFields) &&
                Objects.equals(includedRelations, that.includedRelations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, relationshipFields, includedRelations);
    }

}
