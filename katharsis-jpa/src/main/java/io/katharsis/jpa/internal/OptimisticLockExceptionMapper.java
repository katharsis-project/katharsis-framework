package io.katharsis.jpa.internal;

import java.util.Iterator;

import javax.persistence.OptimisticLockException;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.response.HttpStatus;

public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

	// assign ID do identity among different CONFLICT_409 results
	private static final String JPA_OPTIMISTIC_LOCK_EXCEPTION_ID = "io.katharsis.jpa.optimisticLockException";

	@Override
	public ErrorResponse toErrorResponse(OptimisticLockException cve) {
		ErrorData error = ErrorData.builder().setId(JPA_OPTIMISTIC_LOCK_EXCEPTION_ID).setDetail(cve.getMessage()).build();
		return ErrorResponse.builder().setStatus(HttpStatus.CONFLICT_409).setSingleErrorData(error).build();
	}

	@Override
	public OptimisticLockException fromErrorResponse(ErrorResponse errorResponse) {
		Iterable<ErrorData> errors = errorResponse.getErrors();
		ErrorData error = errors.iterator().next();
		String msg = error.getDetail();
		return new OptimisticLockException(msg);
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		if (errorResponse.getHttpStatus() != HttpStatus.CONFLICT_409) {
			return false;
		}

		Iterable<ErrorData> errors = errorResponse.getErrors();
		Iterator<ErrorData> iterator = errors.iterator();
		if (!iterator.hasNext())
			return false;
		ErrorData errorData = iterator.next();

		String id = errorData.getId();
		return id != null && id.equals(JPA_OPTIMISTIC_LOCK_EXCEPTION_ID);
	}

}
