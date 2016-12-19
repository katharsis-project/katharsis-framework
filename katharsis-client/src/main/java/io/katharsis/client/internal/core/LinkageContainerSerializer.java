package io.katharsis.client.internal.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.utils.PropertyUtils;

import java.io.IOException;

/**
 * Serializes a single linkage object.
 *
 * @see LinkageContainer
 */
public class LinkageContainerSerializer extends JsonSerializer<LinkageContainer> {

    private static final String TYPE_FIELD_NAME = "type";
    private static final String ID_FIELD_NAME = "id";

    private static void writeId(JsonGenerator gen, LinkageContainer linkageContainer) throws IOException {
        ResourceInformation resourceInformation = linkageContainer.getRelationshipEntry().getResourceInformation();
        ResourceField idField = resourceInformation.getIdField();

        // sometimes the entire resource, sometimes only the id is available.
        Object objectItem = linkageContainer.getObjectItem();
        Object sourceId;
        if (!idField.getType().isInstance(objectItem)) {
            sourceId = PropertyUtils.getProperty(linkageContainer.getObjectItem(), idField.getUnderlyingName());
        } else {
            sourceId = objectItem;
        }

        String strSourceId = resourceInformation.toIdString(sourceId);
        gen.writeObjectField(ID_FIELD_NAME, strSourceId);
    }

    @Override
    public void serialize(LinkageContainer linkageContainer, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        writeType(gen, linkageContainer);
        writeId(gen, linkageContainer);
        gen.writeEndObject();
    }

    private void writeType(JsonGenerator gen, LinkageContainer linkageContainer) throws IOException {
        ResourceInformation resourceInformation = linkageContainer.getRelationshipEntry().getResourceInformation();
        gen.writeObjectField(TYPE_FIELD_NAME, resourceInformation.getResourceType());
    }

    public Class<LinkageContainer> handledType() {
        return LinkageContainer.class;
    }
}
