package io.katharsis.resource.exception;

/**
 * A resource does not contain field annotated with JsonApiId annotation.
 */
public class ResourceIdNotFoundException extends ResourceException {

    public ResourceIdNotFoundException(String message) {
        super(message);
    }
}
