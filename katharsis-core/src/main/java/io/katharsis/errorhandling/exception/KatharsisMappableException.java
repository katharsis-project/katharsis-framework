package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;

/**
 * Represents an exception which must be returned to the end user.
 * Consists of error data and related HTTP status, which should be returned in the response.
 */
public abstract class KatharsisMappableException extends KatharsisException {
    private final ErrorData errorData;
    private final int httpStatus;

    protected KatharsisMappableException(int httpStatus, ErrorData errorData) {
        super(errorData.getDetail());
        this.httpStatus = httpStatus;
        this.errorData = errorData;
    }

    public ErrorData getErrorData() {
        return errorData;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
