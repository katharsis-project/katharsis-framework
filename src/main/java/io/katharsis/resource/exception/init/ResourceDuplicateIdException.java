package io.katharsis.resource.exception.init;

import io.katharsis.errorHandling.exception.KatharsisInitalizationException;

/**
 * A resource contains more then one field annotated with JsonApiId annotation.
 */
public final class ResourceDuplicateIdException extends KatharsisInitalizationException {

    public ResourceDuplicateIdException(String className) {
        super("Duplicated Id field found in class: " + className);
    }
}
