package io.katharsis.validation.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorDataBuilder;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	
	private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

	private static final String META_TYPE_VALUE = "ValidationException";

	@Override
	public ErrorResponse toErrorResponse(ValidationException exception) {
		logger.warn("a ValidationException occured", exception);
		
		List<ErrorData> errors = new ArrayList<>();

		ErrorDataBuilder builder = ErrorData.builder();
		builder = builder.addMetaField(ConstraintViolationExceptionMapper.META_TYPE_KEY, META_TYPE_VALUE);
		builder = builder.setStatus(String.valueOf(ConstraintViolationExceptionMapper.UNPROCESSABLE_ENTITY_422));
		builder = builder.setCode(exception.getMessage());
		builder = builder.setTitle(exception.getLocalizedMessage());

		ErrorData error = builder.build();
		errors.add(error);

		return ErrorResponse.builder().setStatus(ConstraintViolationExceptionMapper.UNPROCESSABLE_ENTITY_422).setErrorData(errors)
				.build();
	}

	@Override
	public ValidationException fromErrorResponse(ErrorResponse errorResponse) {
		Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
		String message = null;
		if (errors.hasNext()) {
			ErrorData data = errors.next();
			message = data.getCode();
		}
		return new ValidationException(message);
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		if (errorResponse.getHttpStatus() != ConstraintViolationExceptionMapper.UNPROCESSABLE_ENTITY_422) {
			return false;
		}
		Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
		if (!errors.hasNext()) {
			return false;
		}
		ErrorData error = errors.next();
		Map<String, Object> meta = error.getMeta();
		return meta != null && META_TYPE_VALUE.equals(meta.get(ConstraintViolationExceptionMapper.META_TYPE_KEY));
	}
}
