package io.katharsis.resource.exception;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

/**
 * Thrown when resource for a type cannot be found.
 */
public final class ResourceNotFoundException extends KatharsisMatchingException {

    public ResourceNotFoundException(String path) {
        super(path);
    }
}