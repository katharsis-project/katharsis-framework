package io.katharsis.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.jackson.serializer.ErrorDataDeserializer;
import io.katharsis.jackson.serializer.ErrorDataSerializer;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * Creates Jackson {@link com.fasterxml.jackson.databind.Module} with all custom Katharsis serializers.
 */
public class JsonApiModuleBuilder {

    public static final String JSON_API_MODULE_NAME = "JsonApiModule";

    /**
     * Creates Katharsis Jackson module with all required serializers
     *
     * @param resourceRegistry initialized registry with all of the required resources
     * @param isClient         is katharsis client
     * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
     */
    public SimpleModule build(ResourceRegistry resourceRegistry, boolean isClient) {
        SimpleModule simpleModule = new SimpleModule(JSON_API_MODULE_NAME,
                new Version(1, 0, 0, null, null, null));

        simpleModule.addSerializer(new ErrorDataSerializer());
        simpleModule.addDeserializer(ErrorData.class, new ErrorDataDeserializer());

        return simpleModule;
    }
}
