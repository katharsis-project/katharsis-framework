package io.katharsis.core.internal.exception;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.exception.BadRequestException;
import io.katharsis.errorhandling.exception.ForbiddenException;
import io.katharsis.errorhandling.exception.InternalServerErrorException;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.errorhandling.exception.UnauthorizedException;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.repository.response.HttpStatus;

/**
 * Exception mapper for a generic exception which can be thrown in request processing.
 */
public final class KatharsisExceptionMapper implements ExceptionMapper<KatharsisMappableException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(KatharsisExceptionMapper.class);

	@Override
	public ErrorResponse toErrorResponse(KatharsisMappableException exception) {
		// log 5xx status as error and anything else as warn
		if (exception.getHttpStatus() >= 500 && exception.getHttpStatus() < 600) {
			LOGGER.error("failed to process request", exception);
		}
		else {
			LOGGER.warn("failed to process request", exception);
		}

		return ErrorResponse.builder().setStatus(exception.getHttpStatus()).setSingleErrorData(exception.getErrorData()).build();
	}

	@Override
	public KatharsisMappableException fromErrorResponse(ErrorResponse errorResponse) {
		String message = getMessage(errorResponse);

		int httpStatus = errorResponse.getHttpStatus();
		if (httpStatus == HttpStatus.FORBIDDEN_403) {
			return new ForbiddenException(message);
		}
		if (httpStatus == HttpStatus.UNAUTHORIZED_401) {
			return new UnauthorizedException(message);
		}
		if (httpStatus == HttpStatus.NOT_FOUND_404) {
			return new ResourceNotFoundException(message);
		}
		if (httpStatus == HttpStatus.BAD_REQUEST_400) {
			return new BadRequestException(message);
		}
		if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR_500) {
			return new InternalServerErrorException(message);
		}
		throw new IllegalStateException(errorResponse.toString());
	}

	private String getMessage(ErrorResponse errorResponse) {
		Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
		String message = null;
		if (errors.hasNext()) {
			ErrorData data = errors.next();
			message = data.getDetail();
		}
		return message;
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		int httpStatus = errorResponse.getHttpStatus();
		return  httpStatus == HttpStatus.NOT_FOUND_404 ||
				httpStatus == HttpStatus.BAD_REQUEST_400 || httpStatus == HttpStatus.FORBIDDEN_403
				|| httpStatus == HttpStatus.UNAUTHORIZED_401 || httpStatus == HttpStatus.INTERNAL_SERVER_ERROR_500;
	}
}
