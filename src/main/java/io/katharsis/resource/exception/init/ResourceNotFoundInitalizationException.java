package io.katharsis.resource.exception.init;

import io.katharsis.errorHandling.exception.KatharsisInitalizationException;

public class ResourceNotFoundInitalizationException extends KatharsisInitalizationException {

    public ResourceNotFoundInitalizationException(String className) {
        super("Resource of class not found: " + className);
    }
}
