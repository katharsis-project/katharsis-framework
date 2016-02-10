package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.LinkageContainer;
import io.katharsis.utils.BeanUtils;

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

    @Override
    public void serialize(LinkageContainer linkageContainer, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        writeType(gen, linkageContainer.getRelationshipClass());
        try {
            writeId(gen, linkageContainer);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        gen.writeEndObject();
    }

    private void writeType(JsonGenerator gen, Class<?> relationshipClass) throws IOException {
        String resourceType = resourceRegistry.getResourceType(relationshipClass);
        gen.writeObjectField(TYPE_FIELD_NAME, resourceType);
    }

    private static void writeId(JsonGenerator gen, LinkageContainer linkageContainer)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        ResourceField idField = linkageContainer.getRelationshipEntry().getResourceInformation().getIdField();
        String sourceId = BeanUtils.getProperty(linkageContainer.getObjectItem(), idField.getUnderlyingName());
        gen.writeObjectField(ID_FIELD_NAME, sourceId);
    }

    public Class<LinkageContainer> handledType() {
        return LinkageContainer.class;
    }
}
