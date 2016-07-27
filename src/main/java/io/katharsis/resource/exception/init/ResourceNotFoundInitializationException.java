package io.katharsis.resource.exception.init;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;

public class ResourceNotFoundInitializationException extends KatharsisInitializationException {

    public ResourceNotFoundInitializationException(String className) {
        super("Resource of class not found: " + className);
    }
}
