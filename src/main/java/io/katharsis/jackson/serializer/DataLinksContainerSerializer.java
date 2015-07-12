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
import org.apache.commons.beanutils.BeanUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Serializes a <i>links</i> field of a resource in data field of JSON API response.
 * @see DataLinksContainer
 */
public class DataLinksContainerSerializer extends JsonSerializer<DataLinksContainer> {

    private static final String SELF_FIELD_NAME = "self";
    private static final ResourceFieldNameTransformer RESOURCE_FIELD_NAME_TRANSFORMER = new ResourceFieldNameTransformer();

    private ResourceRegistry resourceRegistry;

    public DataLinksContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(DataLinksContainer dataLinksContainer, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        writeSelfLink(dataLinksContainer, gen);
        writeRelationshipFields(dataLinksContainer, gen);

        gen.writeEndObject();
    }

    private void writeSelfLink(DataLinksContainer dataLinksContainer, JsonGenerator gen) throws IOException {
        Class<?> sourceClass = dataLinksContainer.getData().getClass();
        String resourceUrl = resourceRegistry.getResourceUrl(sourceClass);
        RegistryEntry entry = resourceRegistry.getEntry(sourceClass);
        Field idField = entry.getResourceInformation().getIdField();

        String sourceId;
        try {
            sourceId = BeanUtils.getProperty(dataLinksContainer.getData(), idField.getName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Error writing self link.");
        }
        gen.writeStringField(SELF_FIELD_NAME, resourceUrl + "/" + sourceId);
    }

    private void writeRelationshipFields(DataLinksContainer dataLinksContainer, JsonGenerator gen) throws IOException {
        for (Field field : dataLinksContainer.getRelationshipFields()) {
            RelationshipContainer relationshipContainer = new RelationshipContainer(dataLinksContainer, field);
            gen.writeObjectField(RESOURCE_FIELD_NAME_TRANSFORMER.getName(field), relationshipContainer);
        }
    }

    public Class<DataLinksContainer> handledType() {
        return DataLinksContainer.class;
    }
}
