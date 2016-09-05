package io.katharsis.errorhandling.mapper;

import io.katharsis.errorhandling.ErrorResponse;

public interface ExceptionMapper<E extends Throwable> extends JsonApiExceptionMapper<E> {

	@Override
    ErrorResponse toErrorResponse(E exception);

	/**
	 * Convert the given error response to an exception.
	 *
	 * @param errorResponse error response
	 * @return true if mapper can handle the response
	 */
	E fromErrorResponse(ErrorResponse errorResponse);
	
	boolean accepts(ErrorResponse errorResponse);
}


