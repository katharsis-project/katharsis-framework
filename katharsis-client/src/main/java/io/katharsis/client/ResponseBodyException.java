package io.katharsis.client;

import io.katharsis.errorhandling.exception.KatharsisException;

public class ResponseBodyException extends KatharsisException {

	private static final long serialVersionUID = 824839750617131811L;

	public ResponseBodyException(String message) {
		super(message);
	}
	
	public ResponseBodyException(String message, Exception cause) {
		super(message, cause);
	}
}
