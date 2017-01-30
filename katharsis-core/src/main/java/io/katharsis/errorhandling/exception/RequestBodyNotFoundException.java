package io.katharsis.errorhandling.exception;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.response.HttpStatus;

public class RequestBodyNotFoundException extends BadRequestException {  // NOSONAR exception hierarchy deep but ok

    private static final String TITLE = "Request body not found";

    public RequestBodyNotFoundException(HttpMethod method, String resourceName) {
        super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
            .setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
            .setTitle(TITLE)
            .setDetail("Request body not found, " + method.name() + " method, resource name " + resourceName)
        .build());
    }

}
