package io.katharsis.errorhandling.mapper;

import io.katharsis.errorhandling.ErrorResponse;

/**
 * Use {@link ExceptionMapper} instead which supports katharsis-client as well.
 */
@Deprecated
public interface JsonApiExceptionMapper<E extends Throwable> {

    ErrorResponse toErrorResponse(E exception);
}
