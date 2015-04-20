package io.katharsis.resource.exception;

/**
 * Thrown when resource for a type cannot be found.
 * This exception should be handled by an exception mapper and corresponding 404 HTTP response should be generated.
 */
public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}