package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.RelationshipContainer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

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
        writeLinkage(relationshipContainer, gen);
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

    private void writeLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen) throws IOException {
        Class<?> relationshipClass = relationshipContainer.getRelationshipField().getType();
        RegistryEntry relationshipEntry = resourceRegistry.getEntry(relationshipClass);
        gen.writeFieldName("linkage");
        writeLinkage(relationshipContainer, gen, relationshipClass, relationshipEntry);
    }

    private void writeLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen, Class<?> relationshipClass, RegistryEntry relationshipEntry) throws IOException {
        try {
            if (Iterable.class.isAssignableFrom(relationshipClass)) {
                throw new UnsupportedOperationException("Not implemented");
            } else {
                writeToOneLinkage(relationshipContainer, gen, relationshipClass, relationshipEntry);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing id field", e);
        }
    }

    private void writeToOneLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen, Class<?> relationshipClass, RegistryEntry relationshipEntry)
            throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field relationshipField = relationshipContainer.getRelationshipField();
        Object targetDataObj = PropertyUtils.getProperty(relationshipContainer.getDataLinksContainer().getData(), relationshipField.getName());
        if (targetDataObj == null) {
            gen.writeObject(null);
        } else {
            gen.writeStartObject();
            writeType(gen, relationshipClass);
            writeId(gen, targetDataObj, relationshipEntry.getResourceInformation().getIdField());
            gen.writeEndObject();
        }
    }

    private void writeType(JsonGenerator gen, Class<?> relationshipClass) throws IOException {
        String resourceType = resourceRegistry.getResourceType(relationshipClass);
        gen.writeObjectField("type", resourceType);
    }

    private void writeId(JsonGenerator gen, Object targetDataObj, Field idField)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        String sourceId = BeanUtils.getProperty(targetDataObj, idField.getName());
        gen.writeObjectField("id", sourceId);
    }

    public Class<RelationshipContainer> handledType() {
        return RelationshipContainer.class;
    }
}
