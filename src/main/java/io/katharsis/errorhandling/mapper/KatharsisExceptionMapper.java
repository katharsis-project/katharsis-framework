package io.katharsis.errorhandling.mapper.def;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;

/**
 * Exception mapper for a generic exception which can be thrown in request processing.
 */
public final class KatharsisExceptionMapper implements JsonApiExceptionMapper<KatharsisMappableException> {

    @Override
    public ErrorResponse toErrorResponse(KatharsisMappableException exception) {
        return ErrorResponse.builder()
                .setStatus(exception.getHttpStatus())
                .setSingleErrorData(exception.getErrorData())
                .build();
    }
}
