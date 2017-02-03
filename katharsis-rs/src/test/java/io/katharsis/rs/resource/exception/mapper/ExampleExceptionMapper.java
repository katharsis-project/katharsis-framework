package io.katharsis.rs.resource.exception.mapper;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.legacy.queryParams.errorhandling.ExceptionMapperProvider;
import io.katharsis.repository.response.HttpStatus;
import io.katharsis.rs.resource.exception.ExampleException;

@ExceptionMapperProvider
public class ExampleExceptionMapper implements JsonApiExceptionMapper<ExampleException> {
    @Override
    public ErrorResponse toErrorResponse(ExampleException exception) {
        return ErrorResponse.builder()
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .setSingleErrorData(ErrorData.builder()
                        .setTitle(exception.getTitle())
                        .setId(exception.getId())
                        .build())
                .build();
    }
}
