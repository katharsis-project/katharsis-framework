package io.katharsis.errorHandling.mappers;

import io.katharsis.errorHandling.ErrorObject;
import io.katharsis.errorHandling.ErrorResponse;
import io.katharsis.errorHandling.JsonApiExceptionMapper;
import io.katharsis.repository.RepositoryNotFoundException;

public class RepositoryNotFoundExceptionMapper implements JsonApiExceptionMapper<RepositoryNotFoundException> {
    @Override
    public ErrorResponse toErrorResponse(RepositoryNotFoundException Exception) {
        return ErrorResponse.newBuilder()
                .setStatus(500)
                .setSingleErrorData(ErrorObject.newBuilder()
                        .setTitle("title")
                        .setCode("code")
                        .build())
                .build();
    }
}