package io.katharsis.errorhandling.exception;

/**
 * A field within a resource was not found
 */
public final class ResourceFieldNotFoundException extends KatharsisMatchingException {

    public ResourceFieldNotFoundException(String message) {
        super(message);
    }
}
