package io.katharsis.errorhandling.handlers;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.ErrorResponseBuilder;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;

abstract class AbstractExceptionMapper<T extends Throwable> implements JsonApiExceptionMapper<T> {

  ErrorResponse buildErrorResponse(int statusCode) {
    return new ErrorResponseBuilder()
        .setStatus(statusCode)
        .setSingleErrorData(ErrorData.builder()
            .build())
        .build();
  }

}
