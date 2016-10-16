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
        // log 5xx status as error and anything else as warn
        if (exception.getHttpStatus() >= 500 && exception.getHttpStatus() < 600) {
            logger.error("failed to process request", exception);
        } else {
            logger.warn("failed to process request", exception);
        }

        return ErrorResponse.builder()
                .setStatus(exception.getHttpStatus())
                .setSingleErrorData(exception.getErrorData())
                .build();
    }
}
