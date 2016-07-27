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
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.Generics;
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

    public RelationshipContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(RelationshipContainer relationshipContainer, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
        gen.writeStartObject();
        writeLinks(relationshipContainer, gen);
        if (!relationshipContainer.getRelationshipField().isLazy() ||
            relationshipContainer.isForceInclusion()) {
            writeLinkage(relationshipContainer, gen);
        }
        gen.writeEndObject();
    }

    private void writeLinks(RelationshipContainer relationshipContainer, JsonGenerator gen) throws IOException {
        gen.writeFieldName(LINKS_FIELD_NAME);
        gen.writeStartObject();
        writeLink(relationshipContainer, gen, SELF_FIELD_NAME, true);
        writeLink(relationshipContainer, gen, RELATED_FIELD_NAME, false);
        gen.writeEndObject();
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
        Class relationshipClass = Generics
            .getResourceClass(relationshipContainer.getRelationshipField().getGenericType(), baseClass);
        RegistryEntry relationshipEntry = resourceRegistry.getEntry(relationshipClass);

        gen.writeFieldName(DATA_FIELD_NAME);
        writeLinkageField(relationshipContainer, gen, baseClass, relationshipEntry);
    }

    private void writeLinkageField(RelationshipContainer relationshipContainer, JsonGenerator gen, Class baseClass,
                                   RegistryEntry relationshipEntry)
        throws IOException {
        try {
            if (Iterable.class.isAssignableFrom(baseClass)) {
                writeToManyLinkage(relationshipContainer, gen, relationshipEntry);
            } else {
                writeToOneLinkage(relationshipContainer, gen, relationshipEntry);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Error writing linkage field");
        }
    }

    private static void writeToManyLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen,
                                           RegistryEntry relationshipEntry)
        throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ResourceField relationshipField = relationshipContainer.getRelationshipField();
        Object targetDataObj = PropertyUtils
            .getProperty(relationshipContainer.getDataLinksContainer().getData(), relationshipField.getUnderlyingName());

        gen.writeStartArray();
        if (targetDataObj != null) {
            for (Object objectItem : (Iterable) targetDataObj) {
                Class<?> objectItemClass = ClassUtils.getJsonApiResourceClass(objectItem);
                gen.writeObject(new LinkageContainer(objectItem, objectItemClass, relationshipEntry));
            }
        }
        gen.writeEndArray();
    }

    private static void writeToOneLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen,
                                   RegistryEntry relationshipEntry)
        throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ResourceField relationshipField = relationshipContainer.getRelationshipField();
        Object targetDataObj = PropertyUtils.getProperty(relationshipContainer.getDataLinksContainer().getData(), relationshipField.getUnderlyingName());
        if (targetDataObj == null) {
            gen.writeObject(null);
        } else {
            Class<?> targetDataObjClass = ClassUtils.getJsonApiResourceClass(targetDataObj);
            gen.writeObject(new LinkageContainer(targetDataObj, targetDataObjClass, relationshipEntry));
        }
    }

    public Class<RelationshipContainer> handledType() {
        return RelationshipContainer.class;
    }
}
