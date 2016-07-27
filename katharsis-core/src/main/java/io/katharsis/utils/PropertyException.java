package io.katharsis.utils;

import io.katharsis.errorhandling.exception.KatharsisException;

/**
 * Indicate an exception when accessing resource properties
 */
public class PropertyException extends KatharsisException {

    private final Throwable cause;
    private final Class<?> resourceClass;
    private final String field;

    public PropertyException(Throwable cause, Class<?> resourceClass, String field) {
        super(cause.getMessage());
        this.cause = cause;
        this.resourceClass = resourceClass;
        this.field = field;
    }

    public PropertyException(String message, Class<?> resourceClass, String field) {
        super(message);
        this.cause = this;
        this.resourceClass = resourceClass;
        this.field = field;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public String getField() {
        return field;
    }
}
