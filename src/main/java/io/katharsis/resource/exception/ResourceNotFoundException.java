package io.katharsis.resource.exception;

import io.katharsis.errorHandling.ErrorData;
import io.katharsis.errorHandling.exception.KatharsisException;
import io.katharsis.response.HttpStatus;

import java.util.Arrays;

/**
 * Thrown when resource for a type cannot be found.
 * This exception should be handled by an exception mapper and corresponding 404 HTTP response should be generated.
 */
public final class ResourceNotFoundException extends KatharsisException {

    public static final String TITLE = "Resource not found";

    public ResourceNotFoundException(String resourceName) {
        super(HttpStatus.NOT_FOUND_404, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(resourceName)
                .setStatus(String.valueOf(HttpStatus.NOT_FOUND_404))
                .build());
    }

    public ResourceNotFoundException(Class<?> resourceClass) {
        super(HttpStatus.NOT_FOUND_404, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(resourceClass.getCanonicalName())
                .setStatus(String.valueOf(HttpStatus.NOT_FOUND_404))
                .build());
    }

    public ResourceNotFoundException(String resourceName, String path) {
        super(HttpStatus.NOT_FOUND_404, ErrorData.builder()
                .setTitle(TITLE)
                .setPaths(Arrays.asList(path))
                .setDetail(resourceName)
                .setStatus(String.valueOf(HttpStatus.NOT_FOUND_404))
                .build());
    }
}