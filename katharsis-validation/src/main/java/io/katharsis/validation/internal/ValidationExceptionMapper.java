package io.katharsis.validation.internal;

import javax.validation.ValidationException;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.ExceptionMapperHelper;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	
	private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

	private static final String META_TYPE_VALUE = "ValidationException";

	@Override
	public ErrorResponse toErrorResponse(ValidationException exception) {
		logger.warn("a ValidationException occured", exception);
		
		return ExceptionMapperHelper.toErrorResponse(exception, ConstraintViolationExceptionMapper.UNPROCESSABLE_ENTITY_422,
				META_TYPE_VALUE);
	}

	@Override
	public ValidationException fromErrorResponse(ErrorResponse errorResponse) {
		return new ValidationException(ExceptionMapperHelper.createErrorMessage(errorResponse));
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return ExceptionMapperHelper.accepts(errorResponse, ConstraintViolationExceptionMapper.UNPROCESSABLE_ENTITY_422,
				META_TYPE_VALUE);
	}
}
