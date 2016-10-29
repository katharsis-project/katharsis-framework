package io.katharsis.security;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.response.HttpStatus;

public class ForbiddenException extends KatharsisMappableException {  // NOSONAR exception hierarchy deep but ok

	private static final String TITLE = "FOBIDDEN";

	public ForbiddenException(String message) {
		super(HttpStatus.FORBIDDEN_403, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.FORBIDDEN_403)).build());
	}
}
