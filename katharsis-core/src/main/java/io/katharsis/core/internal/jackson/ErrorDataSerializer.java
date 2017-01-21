package io.katharsis.core.internal.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.katharsis.errorhandling.ErrorData;

/**
 * Serializes top-level Errors object.
 */
public class ErrorDataSerializer extends JsonSerializer<ErrorData> {

	public static final String LINKS = "links";
    public static final String ID = "id";
    public static final String ABOUT_LINK = "about";
    public static final String STATUS = "status";
    public static final String CODE = "code";
    public static final String TITLE = "title";
    public static final String DETAIL = "detail";
    public static final String SOURCE = "source";
    public static final String POINTER = "pointer";
    public static final String PARAMETER = "parameter";
    public static final String META = "meta";

    @Override
    public void serialize(ErrorData errorData, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {

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

    public Class<ErrorData> handledType() {
        return ErrorData.class;
    }

}
