package io.katharsis.errorhandling.exception;

public class ResourceNotFoundInitializationException extends KatharsisInitializationException {

    public ResourceNotFoundInitializationException(String className) {
        super("Resource of class not found: " + className);
    }
}
