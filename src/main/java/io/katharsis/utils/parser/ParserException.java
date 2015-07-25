package io.katharsis.utils.parser;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.response.HttpStatus;

/**
 * Thrown when parser exception occurs.
 */
public class ParserException extends KatharsisMappableException {

    public static final String TITLE = "Type parser error";

    public ParserException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(message)
                .setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500))
                .build());
    }
}
