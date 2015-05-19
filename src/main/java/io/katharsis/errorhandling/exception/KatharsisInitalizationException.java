package io.katharsis.errorhandling.exception;

/**
 * General type for exceptions, which can be thrown during Katharsis startup (building resource registry etc)
 */
public class KatharsisInitalizationException extends RuntimeException {

    public KatharsisInitalizationException(String message) {
        super(message);
    }
}
