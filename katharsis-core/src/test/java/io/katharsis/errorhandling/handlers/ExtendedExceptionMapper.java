package io.katharsis.errorhandling.handlers;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapperProvider;

@ExceptionMapperProvider
public class ExtendedExceptionMapper extends AbstractExceptionMapper<ExtendedExceptionMapper.SomeException> {

  @Override
  public ErrorResponse toErrorResponse(ExtendedExceptionMapper.SomeException Throwable) {
    return buildErrorResponse(404);
  }

  public static class SomeException extends RuntimeException {
  }

}
