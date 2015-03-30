package io.katharsis.resource.exception;

/**
 * A field within a resource was not found
 */
public class ResourceFieldNotFoundException extends ResourceException {
    public ResourceFieldNotFoundException(String message) {
        super(message);
    }
}
