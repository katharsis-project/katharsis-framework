package io.katharsis.resource.exception;

import io.katharsis.errorHandling.ErrorData;
import io.katharsis.errorHandling.exception.KatharsisException;
import io.katharsis.response.HttpStatus;

/**
 * A field within a resource was not found
 */
public final class ResourceFieldNotFoundException extends KatharsisException {

    public static final String TITLE = "Field was not found";

    public ResourceFieldNotFoundException(String fieldName) {
        super(HttpStatus.NOT_FOUND_404, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(fieldName)
                .setStatus(String.valueOf(HttpStatus.NOT_FOUND_404))
                .build());
    }
}
