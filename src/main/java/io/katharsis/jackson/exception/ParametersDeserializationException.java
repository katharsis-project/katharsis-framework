package io.katharsis.jackson.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.response.HttpStatus;

/**
 * Thrown, when is unable to read request parameters
 */
public class ParametersDeserializationException extends KatharsisMappableException {
    private static final String TITLE = "Request parameters error";

    public ParametersDeserializationException(String message) {
        super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(message)
                .setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
                .build());
    }
}
