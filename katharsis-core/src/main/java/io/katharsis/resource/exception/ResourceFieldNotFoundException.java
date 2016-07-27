package io.katharsis.resource.exception;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

/**
 * A field within a resource was not found
 */
public final class ResourceFieldNotFoundException extends KatharsisMatchingException {

    public ResourceFieldNotFoundException(String message) {
        super(message);
    }
}
