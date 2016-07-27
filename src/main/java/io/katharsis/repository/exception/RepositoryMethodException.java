package io.katharsis.repository.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.response.HttpStatus;

public class RepositoryMethodException extends KatharsisMappableException {
    private static final String TITLE = "Resource method error";

    public RepositoryMethodException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder()
            .setTitle(TITLE)
            .setDetail(message)
            .setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500))
            .build());
    }
}
