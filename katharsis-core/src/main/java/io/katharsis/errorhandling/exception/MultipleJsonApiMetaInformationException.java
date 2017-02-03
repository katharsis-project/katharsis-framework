package io.katharsis.errorhandling.exception;

/**
 * A resource contains more then one field annotated with {@link io.katharsis.resource.annotations.JsonApiMetaInformation} annotation.
 */
public class MultipleJsonApiMetaInformationException extends KatharsisInitializationException {

    public MultipleJsonApiMetaInformationException(String className) {
        super("Duplicated meta fields found in class: " + className);
    }
}
