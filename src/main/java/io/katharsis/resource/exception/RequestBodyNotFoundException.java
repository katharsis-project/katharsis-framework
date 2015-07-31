package io.katharsis.resource.exception;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.response.HttpStatus;

public class RequestBodyNotFoundException extends KatharsisMappableException {

    private static final String TITLE = "Request body not found";

    public RequestBodyNotFoundException(HttpMethod method, String resourceName) {
        super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
            .setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
            .setTitle(TITLE)
            .setDetail("Request body not found, " + method.name() + " method, resource name " + resourceName)
        .build());
    }

}
