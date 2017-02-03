package io.katharsis.errorhandling.exception;

/**
 * Indicates that no corresponding controller for a request had not been found.
 */
public class MethodNotFoundException extends KatharsisMatchingException {

    public MethodNotFoundException(String uri, String method) {
        super(String.format("%s: %s", method, uri));
    }
}
