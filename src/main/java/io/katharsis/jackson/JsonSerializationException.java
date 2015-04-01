package io.katharsis.jackson;

import java.io.IOException;

public class JsonSerializationException extends IOException {
    public JsonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
