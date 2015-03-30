package io.katharsis.resource.exception;

/**
 * Thrown when resource for a type cannot be found.
 */
public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}