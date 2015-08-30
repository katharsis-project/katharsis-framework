package io.katharsis.utils.parser;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

/**
 * Thrown when parser exception occurs.
 */
class ParserException extends KatharsisMatchingException {

    public ParserException(String message) {
        super(message);
    }
}
