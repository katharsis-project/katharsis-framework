package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.resource.ResourceFieldNameTransformer;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.DataLinksContainer;
import io.katharsis.response.RelationshipContainer;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Serializes a <i>links</i> field of a resource in data field of JSON API response.
 * @see DataLinksContainer
 */
public class DataLinksContainerSerializer extends JsonSerializer<DataLinksContainer> {


    private static final ResourceFieldNameTransformer RESOURCE_FIELD_NAME_TRANSFORMER = new ResourceFieldNameTransformer();

    private ResourceRegistry resourceRegistry;

    public DataLinksContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(DataLinksContainer dataLinksContainer, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        for (Field field : dataLinksContainer.getRelationshipFields()) {
            RelationshipContainer relationshipContainer = new RelationshipContainer(dataLinksContainer, field);
            gen.writeObjectField(RESOURCE_FIELD_NAME_TRANSFORMER.getName(field), relationshipContainer);
        }

        gen.writeEndObject();
    }

    public Class<DataLinksContainer> handledType() {
        return DataLinksContainer.class;
    }
}
