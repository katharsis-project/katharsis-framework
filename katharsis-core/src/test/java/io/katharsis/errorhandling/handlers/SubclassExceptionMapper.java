package io.katharsis.errorhandling.handlers;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.ErrorResponseBuilder;
import io.katharsis.legacy.queryParams.errorhandling.ExceptionMapperProvider;

/**
 * Created by yuval on 02/03/2017.
 */
@ExceptionMapperProvider
public class SubclassExceptionMapper extends BaseExceptionMapper<IllegalArgumentException> {

  @Override
  public ErrorResponse toErrorResponse(IllegalArgumentException exception) {
    return new ErrorResponseBuilder()
        .setStatus(500)
        .setSingleErrorData(ErrorData.builder()
            .setTitle("byebye")
            .build())
        .build();
  }
}
