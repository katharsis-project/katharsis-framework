package io.katharsis.errorHandling.handlers;

import io.katharsis.errorHandling.*;
import io.katharsis.errorHandling.mapper.ExceptionMapperProvider;
import io.katharsis.errorHandling.mapper.JsonApiExceptionMapper;

@ExceptionMapperProvider
public class SomeExceptionMapper implements JsonApiExceptionMapper<SomeExceptionMapper.SomeException> {

    @Override
    public ErrorResponse toErrorResponse(SomeException Throwable) {
        return new ErrorResponseBuilder()
                .setStatus(500)
                .setSingleErrorData(ErrorData.builder()
                        .setTitle("hello")
                        .build())
                .build();
    }

    public static class SomeException extends RuntimeException {
    }
}

