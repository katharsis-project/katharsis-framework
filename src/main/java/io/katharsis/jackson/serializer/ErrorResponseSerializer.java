package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;

import java.io.IOException;
import java.util.Collections;

/**
 * Serializes top-level Errors object.
 */
public class ErrorResponseSerializer extends JsonSerializer<ErrorResponse> {

    private static final String LINKS = "links";
    private static final String ID = "id";
    private static final String ABOUT_LINK = "about";
    private static final String STATUS = "status";
    private static final String CODE = "code";
    private static final String TITLE = "title";
    private static final String DETAIL = "detail";
    private static final String SOURCE = "source";
    private static final String POINTER = "pointer";
    private static final String PARAMETER = "parameter";
    private static final String META = "meta";

    @Override
    public void serialize(ErrorResponse errorResponse, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {

        gen.writeStartObject();
        serializeErrorCollection(errorResponse, gen);
        gen.writeEndObject();
    }

    private void serializeErrorCollection(ErrorResponse errorResponse, JsonGenerator gen) throws IOException {
        Iterable<ErrorData> values = (Iterable<ErrorData>) errorResponse.getResponse().getEntity();

        if (values == null) {
            values = Collections.emptyList();
        }
        gen.writeArrayFieldStart(ErrorResponse.ERRORS);
        for (ErrorData errorData : values) {
            serializeErrorData(errorData, gen);
        }
        gen.writeEndArray();
    }

    private static void serializeErrorData(ErrorData errorData, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        writeStringIfExists(ID, errorData.getId(), gen);
        writeAboutLink(errorData, gen);
        writeStringIfExists(STATUS, errorData.getStatus(), gen);
        writeStringIfExists(CODE, errorData.getCode(), gen);
        writeStringIfExists(TITLE, errorData.getTitle(), gen);
        writeStringIfExists(DETAIL, errorData.getDetail(), gen);
        writeSource(errorData, gen);
        writeMeta(errorData, gen);
        gen.writeEndObject();
    }

    private static void writeMeta(ErrorData errorData, JsonGenerator gen) throws IOException {
        if (errorData.getMeta() != null) {
            gen.writeObjectField(META, errorData.getMeta());
        }
    }

    private static void writeSource(ErrorData errorData, JsonGenerator gen) throws IOException {
        if (errorData.getSourceParameter() != null || errorData.getSourcePointer() != null) {
            gen.writeObjectFieldStart(SOURCE);
            writeStringIfExists(POINTER, errorData.getSourcePointer(), gen);
            writeStringIfExists(PARAMETER, errorData.getSourceParameter(), gen);
            gen.writeEndObject();
        }
    }

    private static void writeAboutLink(ErrorData errorData, JsonGenerator gen) throws IOException {
        if (errorData.getAboutLink() != null) {
            gen.writeObjectFieldStart(LINKS);
            gen.writeStringField(ABOUT_LINK, errorData.getAboutLink());
            gen.writeEndObject();
        }
    }

    private static void writeStringIfExists(String fieldName, String value, JsonGenerator gen) throws IOException {
        if (value != null) {
            gen.writeStringField(fieldName, value);
        }
    }

    public Class<ErrorResponse> handledType() {
        return ErrorResponse.class;
    }

}
