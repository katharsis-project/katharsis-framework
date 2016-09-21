package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.LinkageContainer;
import io.katharsis.utils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Serializes a single linkage object.
 *
 * @see LinkageContainer
 */
public class LinkageContainerSerializer extends JsonSerializer<LinkageContainer> {

    private static final String TYPE_FIELD_NAME = "type";
    private static final String ID_FIELD_NAME = "id";

    private final ResourceRegistry resourceRegistry;

    public LinkageContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    private static void writeId(JsonGenerator gen, LinkageContainer linkageContainer)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        ResourceInformation resourceInformation = linkageContainer.getRelationshipEntry().getResourceInformation();
        ResourceField idField = resourceInformation.getIdField();

        // sometimes the entire resource, sometimes only the id is available.
        Object objectItem = linkageContainer.getObjectItem();
        Object sourceId;
        if (idField.getType().isInstance(objectItem)) {
            sourceId = objectItem;
        } else {
            sourceId = PropertyUtils.getProperty(linkageContainer.getObjectItem(), idField.getUnderlyingName());
        }

        String strSourceId = resourceInformation.toIdString(sourceId);
        gen.writeObjectField(ID_FIELD_NAME, strSourceId);
    }

    @Override
    public void serialize(LinkageContainer linkageContainer, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        writeType(gen, linkageContainer);
        try {
            writeId(gen, linkageContainer);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        gen.writeEndObject();
    }

    private void writeType(JsonGenerator gen, LinkageContainer linkageContainer) throws IOException {
        if (linkageContainer.getRelationshipEntry() == null) {
            throw new IllegalArgumentException("LinkageContainer must contain RelationshipEntry");
        }
        ResourceInformation resourceInformation = linkageContainer.getRelationshipEntry().getResourceInformation();
        gen.writeObjectField(TYPE_FIELD_NAME, resourceInformation.getResourceType());

    }

    public Class<LinkageContainer> handledType() {
        return LinkageContainer.class;
    }
}
