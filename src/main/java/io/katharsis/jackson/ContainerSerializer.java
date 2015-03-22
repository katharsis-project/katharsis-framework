package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.response.Container;

import java.io.IOException;

public class ContainerSerializer extends JsonSerializer<Container> {
    @Override
    public void serialize(Container value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObject(value.getData());
        gen.writeEndObject();
    }
}
