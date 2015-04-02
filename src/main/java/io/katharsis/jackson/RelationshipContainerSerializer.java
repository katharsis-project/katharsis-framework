package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.RelationshipContainer;
import org.apache.commons.beanutils.BeanUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Serializes a relationship inside of top-level links object
 * <a href="http://jsonapi.org/format/#document-structure-top-level-links">Top-level Links</a>.
 */
public class RelationshipContainerSerializer extends JsonSerializer<RelationshipContainer> {

    private ResourceRegistry resourceRegistry;

    public RelationshipContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(RelationshipContainer relationshipContainer, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        writeLink(relationshipContainer, gen, "self", true);
        writeLink(relationshipContainer, gen, "related", false);
        gen.writeEndObject();
    }

    private void writeLink(RelationshipContainer relationshipContainer, JsonGenerator gen, String fieldName, boolean addLinks) throws IOException {
        Class<?> sourceClass = relationshipContainer.getDataLinksContainer().getData().getClass();
        String resourceUrl = resourceRegistry.getResourceUrl(sourceClass);
        RegistryEntry entry = resourceRegistry.getEntry(sourceClass);
        Field idField = entry.getResourceInformation().getIdField();

        String sourceId;
        try {
            sourceId = BeanUtils.getProperty(relationshipContainer.getDataLinksContainer().getData(), idField.getName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing links", e);
        }
        gen.writeStringField(fieldName, resourceUrl + "/" + sourceId + (addLinks ? "/links/" : "/")
                + relationshipContainer.getRelationshipField().getName());
    }

    public Class<RelationshipContainer> handledType() {
        return RelationshipContainer.class;
    }
}
