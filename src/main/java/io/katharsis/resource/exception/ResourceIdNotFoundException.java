package io.katharsis.resource.exception;

/**
 * A resource does not contain field annotated with JsonApiId annotation.
 */
public class ResourceIdNotFoundException extends RuntimeException {

    public ResourceIdNotFoundException(String message) {
        super(message);
    }
}
