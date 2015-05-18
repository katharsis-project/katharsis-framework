package io.katharsis.resource.exception.init;

import io.katharsis.errorHandling.exception.KatharsisInitalizationException;

public class InvalidResourceException extends KatharsisInitalizationException {

    public InvalidResourceException(String message) {
        super(message);
    }
}
