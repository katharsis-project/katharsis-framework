package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.RelationshipContainer;
import io.katharsis.utils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Serializes a relationship inside of top-level links object
 * <a href="http://jsonapi.org/format/#document-structure-top-level-links">Top-level Links</a>.
 *
 * @see RelationshipContainer
 */
public class RelationshipContainerSerializer extends JsonSerializer<RelationshipContainer> {

    private static final String SELF_FIELD_NAME = "self";
    private static final String RELATED_FIELD_NAME = "related";
    private static final String DATA_FIELD_NAME = "data";
    private static final String LINKS_FIELD_NAME = "links";

    private final ResourceRegistry resourceRegistry;
    private boolean isClient;

    public RelationshipContainerSerializer(ResourceRegistry resourceRegistry, boolean isClient) {
        this.resourceRegistry = resourceRegistry;
        this.isClient = isClient;
    }

    @Override
    public void serialize(RelationshipContainer relationshipContainer, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        writeLinks(relationshipContainer, gen);
        if (relationshipContainer.isForceInclusion()) {
            writeLinkage(relationshipContainer, gen);
        }
        gen.writeEndObject();
    }

    private void writeLinks(RelationshipContainer relationshipContainer, JsonGenerator gen) throws IOException {
        if (!isClient) {
            gen.writeFieldName(LINKS_FIELD_NAME);
            gen.writeStartObject();
            writeLink(relationshipContainer, gen, SELF_FIELD_NAME, true);
            writeLink(relationshipContainer, gen, RELATED_FIELD_NAME, false);
            gen.writeEndObject();
        }
    }

    private void writeLink(RelationshipContainer relationshipContainer, JsonGenerator gen, String fieldName,
                           boolean addLinks) throws IOException {
        Object data = relationshipContainer.getDataLinksContainer().getData();
        Class<?> sourceClass = data.getClass();
        String resourceUrl = resourceRegistry.getResourceUrl(sourceClass);
        RegistryEntry entry = resourceRegistry.getEntry(sourceClass);
        ResourceField idField = entry.getResourceInformation().getIdField();

        Object sourceId = PropertyUtils.getProperty(data, idField.getUnderlyingName());
        String url = resourceUrl + "/" + sourceId + (addLinks ? "/" + PathBuilder.RELATIONSHIP_MARK + "/" : "/")
                + relationshipContainer.getRelationshipField().getJsonName();
        gen.writeStringField(fieldName, url);
    }

    /**
     * Here it is needed to check actual generic type of a class. To achieve that {@code Class::getType} method cannot
     * be used because of type erasure.
     *
     * @param relationshipContainer
     * @param gen
     * @throws IOException
     */
    private void writeLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen) throws IOException {
        Class baseClass = relationshipContainer.getRelationshipField().getType();
        gen.writeFieldName(DATA_FIELD_NAME);
        writeLinkageField(relationshipContainer, gen, baseClass);
    }

    private void writeLinkageField(RelationshipContainer relationshipContainer, JsonGenerator gen, Class baseClass)
            throws IOException {
        try {
            if (Iterable.class.isAssignableFrom(baseClass)) {
                writeToManyLinkage(relationshipContainer, gen);
            } else {
                writeToOneLinkage(relationshipContainer, gen);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Error writing linkage field");
        }
    }

    private void writeToManyLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen)
            throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ResourceField relationshipField = relationshipContainer.getRelationshipField();
        Object targetDataObjects = PropertyUtils
                .getProperty(relationshipContainer.getDataLinksContainer().getData(), relationshipField.getUnderlyingName());

        gen.writeStartArray();
        if (targetDataObjects != null) {
            for (Object targetDataObject : (Iterable) targetDataObjects) {
                Class<?> targetDataObjClass = resourceRegistry.getResourceClass(targetDataObject).get();
                gen.writeObject(new LinkageContainer(targetDataObject, targetDataObjClass, resourceRegistry.getEntry(targetDataObject)));
            }
        }
        gen.writeEndArray();
    }

    private void writeToOneLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen)
            throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ResourceField relationshipField = relationshipContainer.getRelationshipField();
        Object targetDataObject = PropertyUtils.getProperty(relationshipContainer.getDataLinksContainer().getData(), relationshipField.getUnderlyingName());

        if (targetDataObject == null) {
            gen.writeObject(null);
        } else {
            Class<?> targetDataObjClass = resourceRegistry.getResourceClass(targetDataObject).get();
            gen.writeObject(new LinkageContainer(targetDataObject, targetDataObjClass, resourceRegistry.getEntry(targetDataObject)));
        }
    }

    public Class<RelationshipContainer> handledType() {
        return RelationshipContainer.class;
    }
}
