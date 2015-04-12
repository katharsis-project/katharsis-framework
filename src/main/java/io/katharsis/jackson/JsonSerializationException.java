package io.katharsis.jackson;

import java.io.IOException;

/**
 * Thrown when a Jackson serialization related exception occurs.
 */
public class JsonSerializationException extends IOException {
    public JsonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
