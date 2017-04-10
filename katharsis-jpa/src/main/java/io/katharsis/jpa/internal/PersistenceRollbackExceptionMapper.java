package io.katharsis.jpa.internal;

import javax.persistence.RollbackException;

import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.module.Module;
import io.katharsis.module.Module.ModuleContext;
import io.katharsis.utils.Optional;

/**
 * RollbackException can hide the more interesting causes.
 */
public class PersistenceRollbackExceptionMapper implements ExceptionMapper<RollbackException> {

	private ModuleContext context;

	public PersistenceRollbackExceptionMapper(Module.ModuleContext context) {
		this.context = context;
	}

	@Override
	public ErrorResponse toErrorResponse(RollbackException exception) {
		Throwable cause = exception.getCause();
		if (cause != null) {
			Optional<JsonApiExceptionMapper> mapper = context.getExceptionMapperRegistry().findMapperFor(cause.getClass());
			if (mapper.isPresent()) {
				return mapper.get().toErrorResponse(cause);
			}
		}
		return null;
	}

	@Override
	public RollbackException fromErrorResponse(ErrorResponse errorResponse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}

}
