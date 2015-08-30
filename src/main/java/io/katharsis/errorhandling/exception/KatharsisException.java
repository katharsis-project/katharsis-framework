package io.katharsis.errorhandling.exception;

/**
 * General type for exceptions, which can be thrown during Katharsis request processing.
 */
abstract class KatharsisException extends RuntimeException {

    KatharsisException(String message) {
        super(message);
    }
}