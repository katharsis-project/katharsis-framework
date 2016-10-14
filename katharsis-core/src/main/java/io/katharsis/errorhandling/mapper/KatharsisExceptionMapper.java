package io.katharsis.errorhandling.mapper;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception mapper for a generic exception which can be thrown in request processing.
 */
public final class KatharsisExceptionMapper implements JsonApiExceptionMapper<KatharsisMappableException> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ErrorResponse toErrorResponse(KatharsisMappableException exception) {
        logger.warn("failed to process request", exception);
        return ErrorResponse.builder()
                .setStatus(exception.getHttpStatus())
                .setSingleErrorData(exception.getErrorData())
                .build();
    }
}
