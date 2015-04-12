package io.katharsis.jackson.exception;

import java.io.IOException;

public class JsonSerializationException extends IOException {
    public JsonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
