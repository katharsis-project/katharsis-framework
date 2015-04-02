package io.katharsis.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ObjectMapperBuilder {

    public static final String JSON_API_MODULE_NAME = "JsonApiModule";

    public ObjectMapper buildWith(JsonSerializer... serializers) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule(JSON_API_MODULE_NAME,
                new Version(1, 0, 0, null, null, null));

        for (JsonSerializer jsonSerializer : serializers) {
            simpleModule.addSerializer(jsonSerializer);
        }

        objectMapper.registerModule(simpleModule);
        objectMapper.getSerializerProvider();
        return objectMapper;
    }
}
