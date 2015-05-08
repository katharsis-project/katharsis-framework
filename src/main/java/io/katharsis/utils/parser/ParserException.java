package io.katharsis.utils.parser;

/**
 * Thrown when parser exception occurs.
 */
public class ParserException extends RuntimeException {

    public ParserException(String message) {
        super(message);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }
}
