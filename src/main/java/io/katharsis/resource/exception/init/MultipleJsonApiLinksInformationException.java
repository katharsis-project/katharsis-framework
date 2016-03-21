package io.katharsis.resource.exception.init;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;

/**
 * A resource contains more then one field annotated with {@link io.katharsis.resource.annotations.JsonApiLinksInformation} annotation.
 */
public class MultipleJsonApiLinksInformationException extends KatharsisInitializationException {

    public MultipleJsonApiLinksInformationException(String className) {
        super("Duplicated links fields found in class: " + className);
    }
}
