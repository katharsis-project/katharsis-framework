package io.katharsis.core.internal.exception;

import java.util.Set;

import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;

public interface ExceptionMapperLookup {

    Set<JsonApiExceptionMapper> getExceptionMappers();
}
