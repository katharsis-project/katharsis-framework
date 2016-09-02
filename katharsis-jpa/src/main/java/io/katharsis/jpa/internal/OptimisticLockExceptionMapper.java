package io.katharsis.jpa.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.OptimisticLockException;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.response.HttpStatus;

public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

	private static final String META_TYPE_KEY = "type";

	// assign ID do identity among different CONFLICT_409 results
	private static final String JPA_OPTIMISTIC_LOCK_EXCEPTION_TYPE = "OptimisticLockException";

	@Override
	public ErrorResponse toErrorResponse(OptimisticLockException cve) {
		HashMap<String, Object> meta = new HashMap<>();
		meta.put(META_TYPE_KEY, JPA_OPTIMISTIC_LOCK_EXCEPTION_TYPE);

		ErrorData error = ErrorData.builder().setMeta(meta).setDetail(cve.getMessage()).build();
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

		Map<String, Object> meta = errorData.getMeta();
		return meta != null && JPA_OPTIMISTIC_LOCK_EXCEPTION_TYPE.equals(meta.get(META_TYPE_KEY));
	}

}
