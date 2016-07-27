package io.katharsis.errorhandling.exception;

/**
 * General type for exceptions, which can be thrown during Katharsis request processing.
 */
public abstract class KatharsisException extends RuntimeException {

    public KatharsisException(String message) {
        super(message);
    }
}