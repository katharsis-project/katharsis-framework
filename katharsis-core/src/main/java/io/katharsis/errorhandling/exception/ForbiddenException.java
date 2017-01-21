package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.repository.response.HttpStatus;

public class ForbiddenException extends KatharsisMappableException {  // NOSONAR exception hierarchy deep but ok

	private static final String TITLE = "FOBIDDEN";

	public ForbiddenException(String message) {
		super(HttpStatus.FORBIDDEN_403, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.FORBIDDEN_403)).build());
	}
}
