package io.katharsis.errorHandling.mapper.def;

import io.katharsis.errorHandling.ErrorResponse;
import io.katharsis.errorHandling.exception.KatharsisException;
import io.katharsis.errorHandling.mapper.JsonApiExceptionMapper;

/**
 * Exception mapper for a generic exception which can be thrown in request processing.
 */
public final class KatharsisExceptionMapper implements JsonApiExceptionMapper<KatharsisException> {

    @Override
    public ErrorResponse toErrorResponse(KatharsisException exception) {
        return ErrorResponse.builder()
                .setStatus(exception.getHttpStatus())
                .setSingleErrorData(exception.getErrorData())
                .build();
    }
}
