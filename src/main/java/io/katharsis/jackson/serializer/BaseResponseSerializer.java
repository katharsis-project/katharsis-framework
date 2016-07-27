package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.jackson.serializer.include.IncludedRelationshipExtractor;
import io.katharsis.jackson.serializer.include.ResourceDigest;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.Container;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.ResourceResponseContext;

import java.io.IOException;
import java.util.*;

/**
 * Serializes top-level JSON object and provides ability to include compound documents
 */
public class BaseResponseSerializer extends JsonSerializer<BaseResponseContext> {

    private static final String INCLUDED_FIELD_NAME = "included";
    private static final String DATA_FIELD_NAME = "data";
    private static final String META_FIELD_NAME = "meta";
    private static final String LINKS_FIELD_NAME = "links";

    private final IncludedRelationshipExtractor includedRelationshipExtractor;

    public BaseResponseSerializer(ResourceRegistry resourceRegistry) {
        this.includedRelationshipExtractor = new IncludedRelationshipExtractor(resourceRegistry);
    }

    @Override
    public void serialize(BaseResponseContext context, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonApiResponse response = context.getResponse();

        gen.writeStartObject();

        if (isLinkageContainer(context)) {
            gen.writeObjectField(DATA_FIELD_NAME, response.getEntity());
        } else {
            writeResponseWithResources(context, gen);
        }

        if (response.getMetaInformation() != null) {
            gen.writeObjectField(META_FIELD_NAME, response.getMetaInformation());
        }
        if (response.getLinksInformation() != null) {
            gen.writeObjectField(LINKS_FIELD_NAME, response.getLinksInformation());
        }

        gen.writeEndObject();
    }

    private boolean isLinkageContainer(BaseResponseContext context) {
        if (context instanceof ResourceResponseContext) {
            return context.getResponse().getEntity() instanceof LinkageContainer;
        } else if (context instanceof CollectionResponseContext) {
            Iterable data = (Iterable) context.getResponse().getEntity();
            if (data == null) {
                return false;
            } else {
                Iterator iterator = data.iterator();
                return iterator.hasNext() && iterator.next() instanceof LinkageContainer;
            }
        } else {
            throw new IllegalArgumentException(String.format("JsonApiResponse can be either %s or %s. Got %s",
                ResourceResponseContext.class, CollectionResponseContext.class, context.getClass()));
        }
    }

    private void writeResponseWithResources(BaseResponseContext value, JsonGenerator gen) throws IOException {
        Map<ResourceDigest, Container> includedResources;
        if (value instanceof ResourceResponseContext) {
            includedResources = serializeSingle((ResourceResponseContext) value, gen);
        } else if (value instanceof CollectionResponseContext) {
            includedResources = serializeResourceCollection((CollectionResponseContext) value, gen);
        } else {
            throw new IllegalArgumentException(String.format("JsonApiResponse can be either %s or %s. Got %s",
                    ResourceResponseContext.class, CollectionResponseContext.class, value.getClass()));
        }

        gen.writeObjectField(INCLUDED_FIELD_NAME, includedResources.values());
    }

    private Map<ResourceDigest, Container> serializeSingle(ResourceResponseContext responseContext, JsonGenerator gen)
            throws IOException {
        Object value = responseContext.getResponse().getEntity();
        gen.writeObjectField(DATA_FIELD_NAME, new Container(value, responseContext));

        if (value != null) {
            return includedRelationshipExtractor.extractIncludedResources(value, responseContext);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<ResourceDigest, Container> serializeResourceCollection(CollectionResponseContext responseContext, JsonGenerator gen)
            throws IOException {
        Iterable values = (Iterable) responseContext.getResponse().getEntity();
        Map<ResourceDigest, Container> includedFields = new HashMap<>();
        List<Container> containers = new ArrayList<>();

        if (values == null) {
            values = Collections.emptyList();
        }

        for (Object value : values) {
            //noinspection unchecked
            includedFields.putAll(includedRelationshipExtractor.extractIncludedResources(value, responseContext));
            containers.add(new Container(value, responseContext));
        }

        gen.writeObjectField(DATA_FIELD_NAME, containers);

        return includedFields;
    }

    public Class<BaseResponseContext> handledType() {
        return BaseResponseContext.class;
    }
}
