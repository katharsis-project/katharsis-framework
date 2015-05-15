package io.katharsis.errorHandling;

public interface JsonApiExceptionMapper<E extends Throwable> {

    ErrorResponse toErrorResponse(E Throwable);
}
