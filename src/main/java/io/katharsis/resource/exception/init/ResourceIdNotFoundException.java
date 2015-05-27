package io.katharsis.resource.exception.init;

import io.katharsis.errorhandling.exception.KatharsisInitalizationException;

/**
 * A resource does not contain field annotated with JsonApiId annotation.
 */
public final class ResourceIdNotFoundException extends KatharsisInitalizationException {

    public ResourceIdNotFoundException(String className) {
        super("Id field not found in class: " + className);
    }
}
