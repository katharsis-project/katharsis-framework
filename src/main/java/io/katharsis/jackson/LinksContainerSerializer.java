package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.LinksContainer;
import org.apache.commons.beanutils.BeanUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class LinksContainerSerializer extends JsonSerializer<LinksContainer> {

    private ResourceRegistry resourceRegistry;

    public LinksContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(LinksContainer linksContainer, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        Class<?> sourceClass = linksContainer.getData().getClass();
        String resourceUrl = resourceRegistry.getResourceUrl(sourceClass);
        RegistryEntry entry = resourceRegistry.getEntry(sourceClass);
        Field idField = entry.getResourceInformation().getIdField();
        Object sourceId = null;
        try {
            sourceId = BeanUtils.getProperty(linksContainer.getData(), idField.getName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // @todo handle error
            e.printStackTrace();
        }
        gen.writeStringField("self", resourceUrl + "/" + sourceId);
        gen.writeEndObject();
    }

    public Class<LinksContainer> handledType() {
        return LinksContainer.class;
    }
}
