package io.katharsis.response;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

public class DataLinksContainer {
    private final Object data;
    private final Set<Field> relationshipFields;

    public DataLinksContainer(Object data, Set<Field> relationshipFields) {
        this.data = data;
        this.relationshipFields = relationshipFields;
    }

    public Object getData() {
        return data;
    }

    public Set<Field> getRelationshipFields() {
        return relationshipFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataLinksContainer that = (DataLinksContainer) o;
        return Objects.equals(data, that.data) &&
                Objects.equals(relationshipFields, that.relationshipFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, relationshipFields);
    }
}
