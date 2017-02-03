package io.katharsis.core.internal.utils.parser;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

/**
 * Thrown when parser exception occurs.
 */
public class ParserException extends KatharsisMatchingException {

    public ParserException(String message) {
        super(message);
    }
}
