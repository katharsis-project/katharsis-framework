package io.katharsis.client.internal;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;

import io.katharsis.client.ClientException;
import io.katharsis.client.KatharsisClient;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.java.Optional;

public class AbstractStub {

	private static final String CONTENT_TYPE = "application/vnd.api+json";

	protected KatharsisClient katharsis;
	protected RequestUrlBuilder urlBuilder;

	public AbstractStub(KatharsisClient client, RequestUrlBuilder urlBuilder) {
		this.katharsis = client;
		this.urlBuilder = urlBuilder;
	}

	protected BaseResponseContext executeGet(HttpUrl requestUrl) {
		Builder builder = new Request.Builder().url(requestUrl);
		return execute(builder, true);
	}

	protected BaseResponseContext executeDelete(HttpUrl requestUrl) {
		Builder builder = new Request.Builder().url(requestUrl);
		builder.delete();
		return execute(builder, false);
	}

	protected BaseResponseContext execute(Builder builder, boolean getResponse) {
		try {
			Builder complementedBuilder = builder.header("Content-Type", CONTENT_TYPE);

			Request request = complementedBuilder.build();
			Response response = katharsis.getHttpClient().newCall(request).execute();
			if (!response.isSuccessful()) {
				handleError(response);
			}

			String body = response.body().string();
			ObjectMapper objectMapper = katharsis.getObjectMapper();
			if (getResponse) {
				return objectMapper.readValue(body, BaseResponseContext.class);
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void handleError(Response response) throws IOException {
		String body = response.body().string();

		ErrorResponse errorResponse = null;
		if(body != null && body.length() > 0){
			ObjectMapper objectMapper = katharsis.getObjectMapper();
			errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		}
		if(errorResponse != null){
			errorResponse = new ErrorResponse((Iterable<ErrorData>) errorResponse.getResponse().getEntity(), response.code());
		}else{
			errorResponse = new ErrorResponse(null, response.code());
		}
			
		ExceptionMapperRegistry exceptionMapperRegistry = katharsis.getExceptionMapperRegistry();
		Optional<ExceptionMapper<?>> mapper = exceptionMapperRegistry.findMapperFor(errorResponse);
		if(mapper.isPresent()){
			 Throwable throwable = mapper.get().fromErrorResponse(errorResponse);
			 if(throwable instanceof RuntimeException){
				 throw (RuntimeException)throwable;
			 }else{
				 throw new ClientException(response.code(), response.message(), throwable);
			 }
		}else{
			throw new ClientException(response.code(), response.message());
		}
	}
}
