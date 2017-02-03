package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.repository.response.HttpStatus;

public class BadRequestException extends KatharsisMappableException {

	private static final String TITLE = "BAD_REQUEST";

	public BadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST_400, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400)).build());
	}

	public BadRequestException(int httpStatus, ErrorData errorData) {
		super(httpStatus, errorData);
	}

	public BadRequestException(int httpStatus, ErrorData errorData, Throwable cause) {
		super(httpStatus, errorData, cause);
	}
}
