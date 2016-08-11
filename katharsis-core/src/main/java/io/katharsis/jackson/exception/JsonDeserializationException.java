package io.katharsis.jackson.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.response.HttpStatus;

/**
 * Thrown, when is unable to read request body
 */
public class JsonDeserializationException extends KatharsisMappableException {
    private static final String TITLE = "invalid request body";

    public JsonDeserializationException(String message) {
        super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(message)
                .setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
                .build());
    }
}
