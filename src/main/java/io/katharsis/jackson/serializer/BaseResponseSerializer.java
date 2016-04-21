package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.Container;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.ResourceResponseContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        includedRelationshipExtractor = new IncludedRelationshipExtractor(resourceRegistry);
    }

    @Override
    public void serialize(BaseResponseContext context, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Set<?> includedResources = new HashSet<>();
        JsonApiResponse response = context.getResponse();

        gen.writeStartObject();

        if (isLinkageContainer(context)) {
            gen.writeObjectField(DATA_FIELD_NAME, response.getEntity());
        } else {
            writeResponseWithResources(context, gen, includedResources);
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

    private void writeResponseWithResources(BaseResponseContext value, JsonGenerator gen, Set<?> includedResources) throws IOException {
        if (value instanceof ResourceResponseContext) {
            Set included = serializeSingle((ResourceResponseContext) value, gen);
            //noinspection unchecked
            includedResources.addAll(included);
        } else if (value instanceof CollectionResponseContext) {
            Set included = serializeResourceCollection((CollectionResponseContext) value, gen);
            //noinspection unchecked
            includedResources.addAll(included);
        } else {
            throw new IllegalArgumentException(String.format("JsonApiResponse can be either %s or %s. Got %s",
                    ResourceResponseContext.class, CollectionResponseContext.class, value.getClass()));
        }

        gen.writeObjectField(INCLUDED_FIELD_NAME, includedResources);
    }

    private Set<?> serializeSingle(ResourceResponseContext resourceResponseContext, JsonGenerator gen) throws IOException {
        Object value = resourceResponseContext.getResponse().getEntity();
        gen.writeObjectField(DATA_FIELD_NAME, new Container(value, resourceResponseContext));

        if (value != null) {
            return includedRelationshipExtractor.extractIncludedResources(value, resourceResponseContext);
        } else {
            return Collections.emptySet();
        }
    }

    private Set serializeResourceCollection(CollectionResponseContext collectionResponseContext, JsonGenerator gen) throws IOException {
        Iterable values = (Iterable) collectionResponseContext.getResponse().getEntity();
        Set includedFields = new HashSet<>();
        if (values != null) {
            for (Object value : values) {
                //noinspection unchecked
                includedFields.addAll(includedRelationshipExtractor.extractIncludedResources(value, collectionResponseContext));
            }
        } else {
            values = Collections.emptyList();
        }

        List<Container> containers = new LinkedList<>();
        for (Object value : values) {
            containers.add(new Container(value, collectionResponseContext));
        }

        gen.writeObjectField(DATA_FIELD_NAME, containers);

        return includedFields;
    }

    public Class<BaseResponseContext> handledType() {
        return BaseResponseContext.class;
    }
}
