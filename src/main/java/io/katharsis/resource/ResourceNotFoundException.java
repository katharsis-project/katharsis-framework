package io.katharsis.resource;

/**
 * Thrown when resource for a type cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}