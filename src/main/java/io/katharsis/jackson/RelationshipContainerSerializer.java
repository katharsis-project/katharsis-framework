package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.RelationshipContainer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        Class relationshipClass = getResourceClass(relationshipContainer.getRelationshipField(), baseClass);
        RegistryEntry relationshipEntry = resourceRegistry.getEntry(relationshipClass);

        gen.writeFieldName("linkage");
        writeLinkageField(relationshipContainer, gen, baseClass, relationshipClass, relationshipEntry);
    }

    private Class<?> getResourceClass(Field relationshipField, Class baseClass) {
        if (Iterable.class.isAssignableFrom(baseClass)) {
            Type genericFieldType = relationshipField.getGenericType();
            if (genericFieldType instanceof ParameterizedType) {
                ParameterizedType aType = (ParameterizedType) genericFieldType;
                Type[] fieldArgTypes = aType.getActualTypeArguments();
                if (fieldArgTypes.length == 1 && fieldArgTypes[0] instanceof Class<?>) {
                    return (Class) fieldArgTypes[0];
                } else {
                    throw new RuntimeException("Wrong type: " + aType);
                }
            } else {
                throw new RuntimeException("The relationship must be parametrized (cannot be wildcard or array): "
                        + genericFieldType);
            }
        }
        return baseClass;
    }

    private void writeLinkageField(RelationshipContainer relationshipContainer, JsonGenerator gen, Class baseClass,
                                   Class relationshipClass, RegistryEntry relationshipEntry)
            throws IOException {
        try {
            if (Iterable.class.isAssignableFrom(baseClass)) {
                writeToManyLinkage(relationshipContainer, gen, relationshipClass, relationshipEntry);
            } else {
                writeToOneLinkage(relationshipContainer, gen, relationshipClass, relationshipEntry);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing id field", e);
        }
    }

    private void writeToManyLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen, Class relationshipClass, RegistryEntry relationshipEntry)
            throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field relationshipField = relationshipContainer.getRelationshipField();
        Object targetDataObj = PropertyUtils.getProperty(relationshipContainer.getDataLinksContainer().getData(), relationshipField.getName());

        gen.writeStartArray();
        if (targetDataObj != null) {
            for (Object objectItem : (Iterable) targetDataObj) {
                gen.writeObject(new LinkageContainer(objectItem, relationshipClass, relationshipEntry));
            }
        }
        gen.writeEndArray();
    }

    private void writeToOneLinkage(RelationshipContainer relationshipContainer, JsonGenerator gen, Class<?> relationshipClass, RegistryEntry relationshipEntry)
            throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field relationshipField = relationshipContainer.getRelationshipField();
        Object targetDataObj = PropertyUtils.getProperty(relationshipContainer.getDataLinksContainer().getData(), relationshipField.getName());
        if (targetDataObj == null) {
            gen.writeObject(null);
        } else {
            gen.writeObject(new LinkageContainer(targetDataObj, relationshipClass, relationshipEntry));
        }
    }

    public Class<RelationshipContainer> handledType() {
        return RelationshipContainer.class;
    }
}
