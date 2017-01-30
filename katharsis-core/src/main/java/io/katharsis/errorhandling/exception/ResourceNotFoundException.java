package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.repository.response.HttpStatus;

/**
 * Thrown when resource for a type cannot be found.
 */
public final class ResourceNotFoundException extends KatharsisMappableException {

	public ResourceNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND_404, ErrorData.builder().setTitle(message).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.NOT_FOUND_404)).build());
	}

}