package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.Container;
import io.katharsis.response.LinksContainer;
import org.apache.commons.beanutils.BeanUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

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

    private void writeData(JsonGenerator gen, Object data) throws IOException {
        Class<?> dataClass = data.getClass();
        String resourceType = resourceRegistry.getResourceType(dataClass);
        gen.writeStringField("type", resourceType);

        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        try {
            writeId(gen, data, resourceInformation.getIdField());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // @todo handle this error
            e.printStackTrace();
        }

        try {
            writeBasicFields(gen, data, resourceInformation.getBasicFields());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // @todo handle this error
            e.printStackTrace();
        }

        writeRelationshipFields(gen, data, resourceInformation.getRelationshipFields());
    }

    private void writeId(JsonGenerator gen, Object data, Field idField)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        Object sourceId = BeanUtils.getProperty(data, idField.getName());
        gen.writeObjectField("id", sourceId);
    }

    private void writeBasicFields(JsonGenerator gen, Object data, Set<Field> basicFields)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        for (Field basicField : basicFields) {
            Object basicFieldValue = BeanUtils.getProperty(data, basicField.getName());
            gen.writeObjectField(basicField.getName(), basicFieldValue);
        }
    }

    private void writeRelationshipFields(JsonGenerator gen, Object data, Set<Field> relationshipFields) throws IOException {
        LinksContainer linksContainer = new LinksContainer(data, relationshipFields);
        gen.writeObjectField("links", linksContainer);
    }

    public Class<Container> handledType() {
        return Container.class;
    }
}
