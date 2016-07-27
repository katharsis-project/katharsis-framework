package io.katharsis.errorhandling.mapper;

import io.katharsis.errorhandling.ErrorResponse;

public interface JsonApiExceptionMapper<E extends Throwable> {

    ErrorResponse toErrorResponse(E exception);
}
