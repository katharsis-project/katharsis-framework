package io.katharsis.example.springboot.simple.domain.exception;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorDataBuilder;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;

@Component
public class CustomExceptionMapper implements ExceptionMapper<CustomException> {

	private static final int CUSTOM_ERROR_STATUS_CODE = 599;

	@Override
	public ErrorResponse toErrorResponse(CustomException e) {
		ErrorDataBuilder builder = ErrorData.builder();
		builder.setStatus(String.valueOf(CUSTOM_ERROR_STATUS_CODE));
		builder.setTitle(e.getMessage());
		ErrorData error = builder.build();
		List<ErrorData> errors = Arrays.asList(error);
		return ErrorResponse.builder().setStatus(CUSTOM_ERROR_STATUS_CODE).setErrorData(errors).build();
	}

	@Override
	public CustomException fromErrorResponse(ErrorResponse errorResponse) {
		return new CustomException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return errorResponse.getHttpStatus() == CUSTOM_ERROR_STATUS_CODE;
	}

}
