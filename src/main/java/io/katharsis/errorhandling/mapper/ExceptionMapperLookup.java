package io.katharsis.errorhandling.mapper;

import java.util.Set;

public interface ExceptionMapperLookup {

	Set<JsonApiExceptionMapper> getExceptionMappers();
}
