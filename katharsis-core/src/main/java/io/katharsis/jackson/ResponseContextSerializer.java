package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.dispatcher.ResponseContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ResponseContextSerializer extends JsonSerializer<ResponseContext> {

    @Override
    public void serialize(ResponseContext value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeObject(value.getDocument());
    }

    @Override
    public Class<ResponseContext> handledType() {
        return ResponseContext.class;
    }
}
