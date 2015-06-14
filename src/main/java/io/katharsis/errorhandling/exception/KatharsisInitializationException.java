package io.katharsis.errorhandling.exception;

/**
 * General type for exceptions, which can be thrown during Katharsis startup (building resource registry etc)
 */
public class KatharsisInitializationException extends RuntimeException {

    public KatharsisInitializationException(String message) {
        super(message);
    }
}
