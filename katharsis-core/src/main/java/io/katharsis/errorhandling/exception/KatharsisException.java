package io.katharsis.errorhandling.exception;

/**
 * General type for exceptions, which can be thrown during Katharsis request processing.
 */
public abstract class KatharsisException extends RuntimeException {

    public KatharsisException(String message) {
        super(message);
    }

    public KatharsisException(Throwable cause) {
        super(cause);
    }

    public KatharsisException(String message, Throwable cause) {
        super(message, cause);
    }
}