package io.katharsis.dispatcher.registry;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

/**
 * Indicates that no corresponding controller for a request had not been found.
 */
class MethodNotFoundException extends KatharsisMatchingException {

    public MethodNotFoundException(String uri, String method) {
        super(String.format("%s: %s", method, uri));
    }
}
