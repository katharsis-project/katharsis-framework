package io.katharsis.resource.exception.init;

import io.katharsis.errorhandling.exception.KatharsisInitalizationException;

public class InvalidResourceException extends KatharsisInitalizationException {

    public InvalidResourceException(String message) {
        super(message);
    }
}
