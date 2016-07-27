package io.katharsis.resource.exception.init;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;

/**
 * A resource contains more then one field annotated with {@link io.katharsis.resource.annotations.JsonApiMetaInformation} annotation.
 */
public class MultipleJsonApiMetaInformationException extends KatharsisInitializationException {

    public MultipleJsonApiMetaInformationException(String className) {
        super("Duplicated meta fields found in class: " + className);
    }
}
