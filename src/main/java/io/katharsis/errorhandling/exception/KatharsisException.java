package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;

/**
 * General type for exceptions, which can be thrown during Katharsis request processing.
 * Consists of error data and related HTTP status, which should be returned in the response.
 */
public class KatharsisException extends RuntimeException {

    private final ErrorData errorData;
    private final int httpStatus;

    protected KatharsisException(int httpStatus, ErrorData errorData) {
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