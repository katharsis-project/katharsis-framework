package io.katharsis.jackson.exception;

import io.katharsis.errorHandling.ErrorData;
import io.katharsis.errorHandling.exception.KatharsisException;
import io.katharsis.response.HttpStatus;

/**
 * Thrown, when is unable to read request parameters
 */
public class ParametersDeserializationException extends KatharsisException {
public static final String TITLE = "Request parameters error";

    public ParametersDeserializationException(String message) {
        super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(message)
                .setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
                .build());
    }
}
