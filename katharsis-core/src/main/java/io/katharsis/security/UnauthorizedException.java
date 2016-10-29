package io.katharsis.security;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.response.HttpStatus;

public class UnauthorizedException extends KatharsisMappableException {  // NOSONAR exception hierarchy deep but ok

	private static final String TITLE = "UNAUTHORIZED";

	public UnauthorizedException(String message) {
		super(HttpStatus.UNAUTHORIZED_401, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.UNAUTHORIZED_401)).build());
	}
}
