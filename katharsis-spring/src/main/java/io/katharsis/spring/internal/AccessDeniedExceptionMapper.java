package io.katharsis.spring.internal;

import javax.ws.rs.core.Response;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.ExceptionMapperHelper;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import org.springframework.security.access.AccessDeniedException;

/**
 * Mapper class for Spring AccessDeniedExceptions.
 */
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {

	private static final String META_TYPE_VALUE = "AccessDeniedException";
	private static final int ACCESS_DENIED = Response.Status.FORBIDDEN.getStatusCode();

	@Override
	public ErrorResponse toErrorResponse(AccessDeniedException exception) {
		return ExceptionMapperHelper.toErrorResponse(exception, ACCESS_DENIED, META_TYPE_VALUE);
	}

	@Override
	public AccessDeniedException fromErrorResponse(ErrorResponse errorResponse) {
		return new AccessDeniedException(ExceptionMapperHelper.createErrorMessage(errorResponse));
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return ExceptionMapperHelper.accepts(errorResponse, ACCESS_DENIED, META_TYPE_VALUE);
	}
}
