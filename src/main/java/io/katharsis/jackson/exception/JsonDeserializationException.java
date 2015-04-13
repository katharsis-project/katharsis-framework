package io.katharsis.jackson.exception;

import java.io.IOException;

public class JsonDeserializationException extends IOException {
    public JsonDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
