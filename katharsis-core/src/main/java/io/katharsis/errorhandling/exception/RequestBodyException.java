package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.response.HttpStatus;

public class RequestBodyException extends KatharsisMappableException {

    private static final String TITLE = "Request body error";

    public RequestBodyException(@SuppressWarnings("SameParameterValue") HttpMethod method, String resourceName, String details) {
        super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
                .setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
                .setTitle(TITLE)
                .setDetail(String.format("Request body doesn't meet the requirements (%s), %s method, resource name %s",
                        details, method.name(), resourceName))
                .build());
    }
}
