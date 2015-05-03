package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.resource.ResourceInformation;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Serializes top-level JSON object and provides ability to include compound documents
 */
public class BaseResponseSerializer extends JsonSerializer<BaseResponse> {

    private static final String INCLUDED_FIELD_NAME = "included";
    private static final String DATA_FIELD_NAME = "data";

    private ResourceRegistry resourceRegistry;

    public BaseResponseSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(BaseResponse value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        List<?> includedResources = new LinkedList<>();

        gen.writeStartObject();
        if (value instanceof ResourceResponse) {
            List included = serializeSingle(value.getData(), gen);
            includedResources.addAll(included);
        } else if (value instanceof CollectionResponse) {
            List included = serializeResourceCollection(((CollectionResponse) value).getData(), gen);
            includedResources.addAll(included);
        } else {
            throw new IllegalArgumentException(String.format("Response can be either %s or %s. Got %s",
                    ResourceResponse.class, CollectionResponse.class, value.getClass()));
        }

        gen.writeObjectField(INCLUDED_FIELD_NAME, includedResources);

        gen.writeEndObject();
    }

    private List serializeSingle(Object value, JsonGenerator gen) throws IOException {
        gen.writeObjectField(DATA_FIELD_NAME, value);

        if (value instanceof Container && ((Container) value).getData() != null) {
            return extractIncludedResources(((Container) value).getData());
        } else {
            return Collections.emptyList();
        }
    }

    private List<?> extractIncludedResources(Object resource) throws JsonSerializationException {
        Class<?> dataClass = resource.getClass();
        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        Set<Field> relationshipFields = resourceInformation.getRelationshipFields();

        List<?> includedFields = new LinkedList<>();
        for (Field relationshipField : relationshipFields) {
            if (relationshipField.isAnnotationPresent(JsonApiIncludeByDefault.class)) {
                includedFields.addAll(getIncludedFromRelation(relationshipField, resource));
            }
        }
        return includedFields;
    }

    private List getIncludedFromRelation(Field relationshipField, Object resource) throws JsonSerializationException {
        List<Container> includedFields = new LinkedList<>();
        try {
            Object targetDataObj = PropertyUtils.getProperty(resource, relationshipField.getName());
            if (targetDataObj != null) {
                if (Iterable.class.isAssignableFrom(targetDataObj.getClass())) {
                    for (Object objectItem : (Iterable) targetDataObj) {
                        includedFields.add(new Container(objectItem));
                    }
                } else {
                    includedFields.add(new Container(targetDataObj));
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException("Exception while writing id field", e);
        }
        return includedFields;
    }

    private List serializeResourceCollection(Iterable values, JsonGenerator gen) throws IOException {
        List includedFields = new LinkedList<>();
        if (values != null) {
            for (Object value : values) {
                if (value instanceof Container) {
                    includedFields.addAll(extractIncludedResources(((Container) value).getData()));
                }
            }
        } else {
            values = Collections.emptyList();
        }

        gen.writeObjectField(DATA_FIELD_NAME, values);

        return includedFields;
    }

    public Class<BaseResponse> handledType() {
        return BaseResponse.class;
    }
}
