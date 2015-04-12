package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.resource.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.Container;
import io.katharsis.response.DataLinksContainer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * This class serializes an single resource which can be included in <i>data</i> field of JSON API response.
 */
public class ContainerSerializer extends JsonSerializer<Container> {

    private ResourceRegistry resourceRegistry;

    public ContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(Container value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        if (value != null && value.getData() != null) {
            writeData(gen, value.getData());
        }
        gen.writeEndObject();
    }

    /**
     * Writes a value. Each serialized container must contain type field whose value is string
     * <a href="http://jsonapi.org/format/#document-structure-resource-types"></a>.
     */
    private void writeData(JsonGenerator gen, Object data) throws IOException {
        Class<?> dataClass = data.getClass();
        String resourceType = resourceRegistry.getResourceType(dataClass);

        gen.writeStringField("type", resourceType);

        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        try {
            writeId(gen, data, resourceInformation.getIdField());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing id field", e);
        }

        try {
            writeBasicFields(gen, data, resourceInformation.getBasicFields());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing basic fields", e);
        }

        writeRelationshipFields(gen, data, resourceInformation.getRelationshipFields());
    }

    /**
     * The id MUST be written as a string
     * <a href="http://jsonapi.org/format/#document-structure-resource-ids">Resource IDs</a>.
     */
    private void writeId(JsonGenerator gen, Object data, Field idField)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        String sourceId = BeanUtils.getProperty(data, idField.getName());
        gen.writeObjectField("id", sourceId);
    }

    private void writeBasicFields(JsonGenerator gen, Object data, Set<Field> basicFields)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        for (Field basicField : basicFields) {
            Object basicFieldValue = PropertyUtils.getProperty(data, basicField.getName());
            gen.writeObjectField(basicField.getName(), basicFieldValue);
        }
    }

    private void writeRelationshipFields(JsonGenerator gen, Object data, Set<Field> relationshipFields) throws IOException {
        DataLinksContainer dataLinksContainer = new DataLinksContainer(data, relationshipFields);
        gen.writeObjectField("links", dataLinksContainer);
    }

    public Class<Container> handledType() {
        return Container.class;
    }
}
