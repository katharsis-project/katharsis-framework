package io.katharsis.errorHandling.mapper;

import io.katharsis.errorHandling.ErrorResponse;

public interface JsonApiExceptionMapper<E extends Throwable> {

    ErrorResponse toErrorResponse(E exception);
}
