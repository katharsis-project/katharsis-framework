package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Serializes top-level JSON object and provides ability to include compound documents
 */
public class BaseResponseSerializer extends JsonSerializer<BaseResponse> {

    private static final String INCLUDED_FIELD_NAME = "included";
    private static final String DATA_FIELD_NAME = "data";

    private ResourceRegistry resourceRegistry;
    private IncludedRelationshipExtractor includedRelationshipExtractor;

    public BaseResponseSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;

        includedRelationshipExtractor = new IncludedRelationshipExtractor();
    }

    @Override
    public void serialize(BaseResponse value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Set<?> includedResources = new HashSet<>();

        gen.writeStartObject();
        if (value instanceof ResourceResponse) {
            Set included = serializeSingle((ResourceResponse) value, gen);
            includedResources.addAll(included);
        } else if (value instanceof CollectionResponse) {
            Set included = serializeResourceCollection((CollectionResponse) value, gen);
            includedResources.addAll(included);
        } else {
            throw new IllegalArgumentException(String.format("Response can be either %s or %s. Got %s",
                    ResourceResponse.class, CollectionResponse.class, value.getClass()));
        }

        gen.writeObjectField(INCLUDED_FIELD_NAME, includedResources);

        gen.writeEndObject();
    }

    private Set serializeSingle(ResourceResponse resourceResponse, JsonGenerator gen) throws IOException {
        Object value = resourceResponse.getData();
        gen.writeObjectField(DATA_FIELD_NAME, new Container(value));

        if (value != null) {
            Set<Field> relationshipFields = getRelationshipFields(value);
            return includedRelationshipExtractor.extractIncludedResources(value, relationshipFields, resourceResponse);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    private Set<Field> getRelationshipFields(Object resource) {
        Class<?> dataClass = resource.getClass();
        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        return resourceInformation.getRelationshipFields();
    }

    private Set serializeResourceCollection(CollectionResponse collectionResponse, JsonGenerator gen) throws IOException {
        Iterable values = collectionResponse.getData();
        Set includedFields = new HashSet<>();
        if (values != null) {
            for (Object value : values) {
                Set<Field> relationshipFields = getRelationshipFields(value);
                includedFields.addAll(includedRelationshipExtractor.extractIncludedResources(value, relationshipFields, collectionResponse));
            }
        } else {
            values = Collections.emptyList();
        }

        List<Container> containers = new LinkedList<>();
        for (Object value : values) {
            containers.add(new Container(value));
        }

        gen.writeObjectField(DATA_FIELD_NAME, containers);

        return includedFields;
    }

    public Class<BaseResponse> handledType() {
        return BaseResponse.class;
    }
}
