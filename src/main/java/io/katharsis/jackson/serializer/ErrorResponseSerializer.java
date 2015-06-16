package io.katharsis.jackson.serializer;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializes top-level Errors object.
 */
public class ErrorResponseSerializer extends JsonSerializer<ErrorResponse> {

    private static final String ERRORS_FIELD_NAME = "errors";

    public ErrorResponseSerializer() {
    }

    @Override
    public void serialize(ErrorResponse value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {

        gen.writeStartObject();

        serializeErrorCollection(value, gen);

        gen.writeEndObject();
    }

    private void serializeErrorCollection(ErrorResponse errorResponse, JsonGenerator gen) throws IOException {
        Iterable<ErrorData> values = errorResponse.getData();

        if (values == null) {
            values = Collections.emptyList();
        }

        gen.writeObjectField(ERRORS_FIELD_NAME, values);
    }

    public Class<ErrorResponse> handledType() {
        return ErrorResponse.class;
    }

}
