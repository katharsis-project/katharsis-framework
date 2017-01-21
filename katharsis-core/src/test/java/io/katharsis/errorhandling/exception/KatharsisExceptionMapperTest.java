package io.katharsis.errorhandling.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Test;

import io.katharsis.core.internal.exception.KatharsisExceptionMapper;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.repository.response.HttpStatus;

public class KatharsisExceptionMapperTest {

	private static final String TITLE1 = "title1";

	private static final String DETAIL1 = "detail1";

	@Test
	public void shouldMapToErrorResponse() throws Exception {
		KatharsisExceptionMapper mapper = new KatharsisExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new SampleKatharsisException());

		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
		assertThat((Iterable<?>) response.getResponse().getEntity()).hasSize(1).extracting("title", "detail")
				.containsExactly(tuple(TITLE1, DETAIL1));
	}

	@Test
	public void internalServerError() {
		KatharsisExceptionMapper mapper = new KatharsisExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new InternalServerErrorException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
		assertThat(mapper.accepts(response)).isTrue();
		KatharsisMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(InternalServerErrorException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}


	@Test(expected=IllegalStateException.class)
	public void invalidExceptionNotManagedByMapper() {
		KatharsisExceptionMapper mapper = new KatharsisExceptionMapper();
		mapper.fromErrorResponse(new ErrorResponse(null, 123));
	}

	@Test
	public void badRequest() {
		KatharsisExceptionMapper mapper = new KatharsisExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new BadRequestException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
		assertThat(mapper.accepts(response)).isTrue();
		KatharsisMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(BadRequestException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}

	@Test
	public void notAuthorized() {
		KatharsisExceptionMapper mapper = new KatharsisExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new UnauthorizedException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
		assertThat(mapper.accepts(response)).isTrue();
		KatharsisMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(UnauthorizedException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}

	@Test
	public void forbidden() {
		KatharsisExceptionMapper mapper = new KatharsisExceptionMapper();
		ErrorResponse response = mapper.toErrorResponse(new ForbiddenException("testMessage"));
		assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
		assertThat(mapper.accepts(response)).isTrue();
		KatharsisMappableException exception = mapper.fromErrorResponse(response);
		assertThat(exception).isInstanceOf(ForbiddenException.class);
		assertThat(exception.getMessage()).isEqualTo("testMessage");
	}

	private static class SampleKatharsisException extends KatharsisMappableException {

		SampleKatharsisException() {
			super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder().setTitle(TITLE1).setDetail(DETAIL1)
					.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500)).build());
		}
	}
}