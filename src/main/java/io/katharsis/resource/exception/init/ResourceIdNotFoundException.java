package io.katharsis.resource.exception.init;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;

/**
 * A resource does not contain field annotated with JsonApiId annotation.
 */
public final class ResourceIdNotFoundException extends KatharsisInitializationException {

    public ResourceIdNotFoundException(String className) {
        super("Id field not found in class: " + className);
    }
}
