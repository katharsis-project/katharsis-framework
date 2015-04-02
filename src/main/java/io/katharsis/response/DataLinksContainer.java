package io.katharsis.response;

import java.lang.reflect.Field;
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
}
