package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.repository.response.HttpStatus;

/**
 * General exception regarding resource building.
 */
public class ResourceException extends InternalServerErrorException {  // NOSONAR exception hierarchy deep but ok

    private static final String TITLE = "Resource error";

    public ResourceException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(message)
                .setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500))
                .build());
    }
    
    public ResourceException(String message, Exception cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(message)
                .setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500))
                .build(), cause);
    }
}
