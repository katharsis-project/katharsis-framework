package io.katharsis.errorhandling.handlers;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;

public class NoAnnotationExceptionMapper implements JsonApiExceptionMapper<NoAnnotationExceptionMapper.ShouldNotAppearException> {
    @Override
    public ErrorResponse toErrorResponse(ShouldNotAppearException exception) {
        return ErrorResponse.builder().setStatus(500).build();
    }

    public static class ShouldNotAppearException extends RuntimeException {
    }
}
