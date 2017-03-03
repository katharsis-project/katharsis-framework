package io.katharsis.rs.internal;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.errorhandling.ExceptionMapperHelper;
import io.katharsis.rs.type.JsonApiMediaType;
import io.katharsis.utils.Optional;

/**
 * Maps exceptions for which a Katharsis exception mapper has been registered in
 * the Katharsis {@link ExceptionMapperRegistry} to proper JSON API responses.
 */
public class JsonApiExceptionMapper implements ExceptionMapper<Throwable> {

	private ExceptionMapperRegistry exceptionMapperRegistry;

	JsonApiExceptionMapper(ExceptionMapperRegistry exceptionMapperRegistry) {
		this.exceptionMapperRegistry = exceptionMapperRegistry;
	}

	/**
	 * Maps any given exception for which an exception mapper has been registered in the
	 * Katharsis {@link ExceptionMapperRegistry} to a JSON API response.
	 * @param exception The exception to be mapped to JSON
	 * @return A JAX-RS response containing a JSON message describing the exception
	 */
	@Override
	public Response toResponse(Throwable exception) {
		Optional<io.katharsis.errorhandling.mapper.JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(exception.getClass());
		io.katharsis.repository.response.Response errorResponse;
		if (exceptionMapper.isPresent()) {
			errorResponse = exceptionMapper.get().toErrorResponse(exception).toResponse();
		}
		else {
			int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
			errorResponse = ExceptionMapperHelper.toErrorResponse(exception, statusCode, exception.getClass().getName()).toResponse();
		}

		return Response.status(errorResponse.getHttpStatus()).entity(errorResponse.getDocument())
				.header("Content-Type", JsonApiMediaType.APPLICATION_JSON_API).build();
	}

}
