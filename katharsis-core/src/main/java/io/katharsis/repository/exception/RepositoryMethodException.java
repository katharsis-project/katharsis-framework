package io.katharsis.repository.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.InternalServerErrorException;
import io.katharsis.response.HttpStatus;

public class RepositoryMethodException extends InternalServerErrorException {  // NOSONAR exception hierarchy deep but ok
    private static final String TITLE = "Resource method error";

    public RepositoryMethodException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder()
            .setTitle(TITLE)
            .setDetail(message)
            .setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500))
            .build());
    }
}
