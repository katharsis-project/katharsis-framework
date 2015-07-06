package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.request.dto.Attributes;
import io.katharsis.resource.ResourceFieldNameTransformer;
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
 *
 * @see Container
 */
public class ContainerSerializer extends JsonSerializer<Container> {

    private static final String TYPE_FIELD_NAME = "type";
    private static final String ID_FIELD_NAME = "id";
    private static final String ATTRIBUTES_FIELD_NAME = "attributes";
    private static final String RELATIONSHIPS_FIELD_NAME = "relationships";
    private static final String LINKS_FIELD_NAME = "links";
    private static final String SELF_FIELD_NAME = "self";
    private static final ResourceFieldNameTransformer RESOURCE_FIELD_NAME_TRANSFORMER = new ResourceFieldNameTransformer();

    private ResourceRegistry resourceRegistry;

    public ContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(Container value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        if (value != null && value.getData() != null) {
            gen.writeStartObject();
            writeData(gen, value.getData());
            gen.writeEndObject();
        } else {
            gen.writeObject(null);
        }
    }

    /**
     * Writes a value. Each serialized container must contain type field whose value is string
     * <a href="http://jsonapi.org/format/#document-structure-resource-types"></a>.
     */
    private void writeData(JsonGenerator gen, Object data) throws IOException {
        Class<?> dataClass = data.getClass();
        String resourceType = resourceRegistry.getResourceType(dataClass);

        gen.writeStringField(TYPE_FIELD_NAME, resourceType);

        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        try {
            writeId(gen, data, resourceInformation.getIdField());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing id field", e);
        }

        try {
            writeAttributes(gen, data, resourceInformation.getAttributeFields());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing basic fields", e);
        }

        writeRelationshipFields(gen, data, resourceInformation.getRelationshipFields());
        writeLinksField(gen, data);
    }

    /**
     * The id MUST be written as a string
     * <a href="http://jsonapi.org/format/#document-structure-resource-ids">Resource IDs</a>.
     */
    private void writeId(JsonGenerator gen, Object data, Field idField)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        String sourceId = BeanUtils.getProperty(data, idField.getName());
        gen.writeObjectField(ID_FIELD_NAME, sourceId);
    }

    private void writeAttributes(JsonGenerator gen, Object data, Set<Field> attributeFields)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {

        Attributes attributesObject = new Attributes();
        for (Field attributeField : attributeFields) {
            if (!attributeField.isSynthetic()) {
                Object basicFieldValue = PropertyUtils.getProperty(data, attributeField.getName());
                attributesObject.addAttribute(RESOURCE_FIELD_NAME_TRANSFORMER.getName(attributeField), basicFieldValue);
            }
        }
        gen.writeObjectField(ATTRIBUTES_FIELD_NAME, attributesObject);
    }

    private void writeRelationshipFields(JsonGenerator gen, Object data, Set<Field> relationshipFields) throws IOException {
        DataLinksContainer dataLinksContainer = new DataLinksContainer(data, relationshipFields);
        gen.writeObjectField(RELATIONSHIPS_FIELD_NAME, dataLinksContainer);
    }

    private void writeLinksField(JsonGenerator gen, Object data) throws IOException {
        gen.writeFieldName(LINKS_FIELD_NAME);
        gen.writeStartObject();
        writeSelfLink(gen, data);
        gen.writeEndObject();
    }

    private void writeSelfLink(JsonGenerator gen, Object data) throws IOException {
        Class<?> sourceClass = data.getClass();
        String resourceUrl = resourceRegistry.getResourceUrl(sourceClass);
        RegistryEntry entry = resourceRegistry.getEntry(sourceClass);
        Field idField = entry.getResourceInformation().getIdField();

        String sourceId;
        try {
            sourceId = BeanUtils.getProperty(data, idField.getName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing links", e);
        }
        gen.writeStringField(SELF_FIELD_NAME, resourceUrl + "/" + sourceId);
    }

    public Class<Container> handledType() {
        return Container.class;
    }
}
