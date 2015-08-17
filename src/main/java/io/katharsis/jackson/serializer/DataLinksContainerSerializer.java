package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.ResourceField;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.DataLinksContainer;
import io.katharsis.response.RelationshipContainer;

import java.io.IOException;

/**
 * Serializes a <i>links</i> field of a resource in data field of JSON API response.
 * @see DataLinksContainer
 */
public class DataLinksContainerSerializer extends JsonSerializer<DataLinksContainer> {

    private ResourceRegistry resourceRegistry;

    public DataLinksContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(DataLinksContainer dataLinksContainer, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        for (ResourceField field : dataLinksContainer.getRelationshipFields()) {
            RelationshipContainer relationshipContainer = new RelationshipContainer(dataLinksContainer, field);
            gen.writeObjectField(field.getName(), relationshipContainer);
        }

        gen.writeEndObject();
    }

    public Class<DataLinksContainer> handledType() {
        return DataLinksContainer.class;
    }
}
