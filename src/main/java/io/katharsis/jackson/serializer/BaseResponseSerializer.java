package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.*;

import java.io.IOException;
import java.util.*;

/**
 * Serializes top-level JSON object and provides ability to include compound documents
 */
public class BaseResponseSerializer extends JsonSerializer<BaseResponse> {

    private static final String INCLUDED_FIELD_NAME = "included";
    private static final String DATA_FIELD_NAME = "data";
    private static final String META_FIELD_NAME = "meta";
    private static final String LINKS_FIELD_NAME = "links";

    private final IncludedRelationshipExtractor includedRelationshipExtractor;

    public BaseResponseSerializer(ResourceRegistry resourceRegistry) {
        includedRelationshipExtractor = new IncludedRelationshipExtractor(resourceRegistry);
    }

    @Override
    public void serialize(BaseResponse value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Set<?> includedResources = new HashSet<>();

        gen.writeStartObject();

        if (isLinkageContainer(value)) {
            gen.writeObjectField(DATA_FIELD_NAME, value.getData());
        } else {
            writeResponseWithResources(value, gen, includedResources);
        }

        if (value.getMetaInformation() != null) {
            gen.writeObjectField(META_FIELD_NAME, value.getMetaInformation());
        }
        if (value.getLinksInformation() != null) {
            gen.writeObjectField(LINKS_FIELD_NAME, value.getLinksInformation());
        }

        gen.writeEndObject();
    }

    private boolean isLinkageContainer(BaseResponse value) {
        if (value instanceof ResourceResponse) {
            return value.getData() instanceof LinkageContainer;
        } else if (value instanceof CollectionResponse) {
            Iterable data = ((CollectionResponse) value).getData();
            if (data == null) {
                return false;
            } else {
                Iterator iterator = data.iterator();
                return iterator.hasNext() && iterator.next() instanceof LinkageContainer;
            }
        } else {
            throw new IllegalArgumentException(String.format("Response can be either %s or %s. Got %s",
                ResourceResponse.class, CollectionResponse.class, value.getClass()));
        }
    }

    private void writeResponseWithResources(BaseResponse value, JsonGenerator gen, Set<?> includedResources) throws IOException {
        if (value instanceof ResourceResponse) {
            Set included = serializeSingle((ResourceResponse) value, gen);
            //noinspection unchecked
            includedResources.addAll(included);
        } else if (value instanceof CollectionResponse) {
            Set included = serializeResourceCollection((CollectionResponse) value, gen);
            //noinspection unchecked
            includedResources.addAll(included);
        } else {
            throw new IllegalArgumentException(String.format("Response can be either %s or %s. Got %s",
                    ResourceResponse.class, CollectionResponse.class, value.getClass()));
        }

        gen.writeObjectField(INCLUDED_FIELD_NAME, includedResources);
    }

    private Set<?> serializeSingle(ResourceResponse resourceResponse, JsonGenerator gen) throws IOException {
        Object value = resourceResponse.getData();
        gen.writeObjectField(DATA_FIELD_NAME, new Container(value, resourceResponse));

        if (value != null) {
            return includedRelationshipExtractor.extractIncludedResources(value, resourceResponse);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    private Set serializeResourceCollection(CollectionResponse collectionResponse, JsonGenerator gen) throws IOException {
        Iterable values = collectionResponse.getData();
        Set includedFields = new HashSet<>();
        if (values != null) {
            for (Object value : values) {
                //noinspection unchecked
                includedFields.addAll(includedRelationshipExtractor.extractIncludedResources(value, collectionResponse));
            }
        } else {
            values = Collections.emptyList();
        }

        List<Container> containers = new LinkedList<>();
        for (Object value : values) {
            containers.add(new Container(value, collectionResponse));
        }

        gen.writeObjectField(DATA_FIELD_NAME, containers);

        return includedFields;
    }

    public Class<BaseResponse> handledType() {
        return BaseResponse.class;
    }
}
