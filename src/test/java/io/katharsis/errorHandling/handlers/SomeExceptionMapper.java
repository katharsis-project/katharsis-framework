package io.katharsis.errorHandling.handlers;

import io.katharsis.errorHandling.*;

@ExceptionMapperProvider
public class SomeExceptionMapper implements JsonApiExceptionMapper<SomeExceptionMapper.SomeException> {

    @Override
    public ErrorResponse toErrorResponse(SomeException Throwable) {
        return new ErrorResponseBuilder()
                .setStatus(500)
                .setSingleErrorData(ErrorObject.newBuilder()
                        .setTitle("hello")
                        .build())
                .build();
    }

    public static class SomeException extends RuntimeException {
    }
}

