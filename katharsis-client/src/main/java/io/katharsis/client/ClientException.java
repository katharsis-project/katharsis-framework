package io.katharsis.client;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;

/**
 * General client exception if no custom mapper is found.
 */
public class ClientException extends KatharsisMappableException {

	private static final long serialVersionUID = 7455315058615968760L;
	
	private static final String TITLE = "Response error";

	public ClientException(int code, String message) {
		this(code, message, null);
	}
	
    public ClientException(int code, String message, Throwable cause) {
        super(code, ErrorData.builder()
                .setTitle(TITLE)
                .setDetail(message)
                .setStatus(String.valueOf(code))
                .build());
        if(cause != null){
        	initCause(cause);
        }
    }
}
