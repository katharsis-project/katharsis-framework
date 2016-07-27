package io.katharsis.errorhandling.exception;

/**
 * Defines internal Katharsis exception
 */
public class InternalException extends KatharsisInitializationException {

    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Exception e) {
        super(message, e);
    }
}
