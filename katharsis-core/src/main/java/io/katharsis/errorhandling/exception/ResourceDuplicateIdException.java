package io.katharsis.errorhandling.exception;

/**
 * A resource contains more then one field annotated with JsonApiId annotation.
 */
public final class ResourceDuplicateIdException extends KatharsisInitializationException {

    public ResourceDuplicateIdException(String className) {
        super("Duplicated Id field found in class: " + className);
    }
}
