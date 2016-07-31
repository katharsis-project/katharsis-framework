package io.katharsis.dispatcher;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.request.Request;

/**
 * Indicates that no corresponding controller for a request had not been found.
 */
public class MethodNotFoundException extends KatharsisMatchingException {

    public MethodNotFoundException(Request request) {
        super(request.toString());
    }
}
