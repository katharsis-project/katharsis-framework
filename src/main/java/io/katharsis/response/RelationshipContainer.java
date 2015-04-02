package io.katharsis.response;

import java.lang.reflect.Field;

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
}
