package io.katharsis.errorhandling;

import io.katharsis.errorhandling.exception.KatharsisException;

public class GenericKatharsisException extends KatharsisException {
    public GenericKatharsisException(String message) {
        super(message);
    }
}
