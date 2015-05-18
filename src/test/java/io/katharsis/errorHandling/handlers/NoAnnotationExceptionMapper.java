package io.katharsis.errorHandling.handlers;

import io.katharsis.errorHandling.ErrorResponse;
import io.katharsis.errorHandling.mapper.JsonApiExceptionMapper;

public class NoAnnotationExceptionMapper implements JsonApiExceptionMapper<NoAnnotationExceptionMapper.ShouldNotAppearException> {
    @Override
    public ErrorResponse toErrorResponse(ShouldNotAppearException exception) {
        return ErrorResponse.builder().setStatus(500).build();
    }

    public static class ShouldNotAppearException extends RuntimeException {
    }
}
