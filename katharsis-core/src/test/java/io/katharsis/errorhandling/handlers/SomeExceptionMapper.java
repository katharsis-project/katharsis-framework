package io.katharsis.errorhandling.handlers;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.ErrorResponseBuilder;
import io.katharsis.errorhandling.mapper.ExceptionMapperProvider;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;

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

