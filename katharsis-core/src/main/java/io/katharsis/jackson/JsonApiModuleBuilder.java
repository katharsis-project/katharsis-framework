package io.katharsis.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.katharsis.jackson.deserializer.RequestBodyDeserializer;
import io.katharsis.jackson.deserializer.ResourceRelationshipsDeserializer;
import io.katharsis.jackson.serializer.BaseResponseSerializer;
import io.katharsis.jackson.serializer.ContainerSerializer;
import io.katharsis.jackson.serializer.DataLinksContainerSerializer;
import io.katharsis.jackson.serializer.ErrorResponseSerializer;
import io.katharsis.jackson.serializer.LinkageContainerSerializer;
import io.katharsis.jackson.serializer.RelationshipContainerSerializer;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * Creates Jackson {@link com.fasterxml.jackson.databind.Module} with all custom Katharsis serializers.
 */
public class JsonApiModuleBuilder {

    public static final String JSON_API_MODULE_NAME = "JsonApiModule";

    public static SimpleModule create() {
        SimpleModule simpleModule = new SimpleModule(JSON_API_MODULE_NAME,
                new Version(1, 0, 0, null, null, null));

        simpleModule
                .addSerializer(new ErrorResponseSerializer())
                .addSerializer(new ResponseContextSerializer())
                .addSerializer(new DataResponseSerializer())
                .addDeserializer(ResourceRelationships.class, new ResourceRelationshipsDeserializer())
                .addDeserializer(RequestBody.class, new RequestBodyDeserializer());


        return simpleModule;
    }

    /**
     * Creates Katharsis Jackson module with all required serializers
     *
     * @param resourceRegistry initialized registry with all of the required resources
     * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
     */
    public SimpleModule build(ResourceRegistry resourceRegistry) {
        SimpleModule simpleModule = new SimpleModule(JSON_API_MODULE_NAME,
                new Version(1, 0, 0, null, null, null));

        simpleModule.addSerializer(new ContainerSerializer(resourceRegistry))
                .addSerializer(new DataLinksContainerSerializer(resourceRegistry))
                .addSerializer(new RelationshipContainerSerializer(resourceRegistry))
                .addSerializer(new LinkageContainerSerializer(resourceRegistry))
                .addSerializer(new BaseResponseSerializer(resourceRegistry))
                .addSerializer(new ErrorResponseSerializer())
                .addSerializer(new DataResponseSerializer())
                .addDeserializer(ResourceRelationships.class, new ResourceRelationshipsDeserializer())
                .addDeserializer(RequestBody.class, new RequestBodyDeserializer());

        return simpleModule;
    }
}
