package io.katharsis.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;
import io.katharsis.client.ClientException;
import io.katharsis.client.KatharsisClient;
import io.katharsis.client.response.ResourceList;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.utils.JsonApiUrlBuilder;
import io.katharsis.utils.java.Optional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AbstractStub {

	private static final String CONTENT_TYPE = "application/vnd.api+json";

	protected KatharsisClient katharsis;
	protected JsonApiUrlBuilder urlBuilder;

	public AbstractStub(KatharsisClient client, JsonApiUrlBuilder urlBuilder) {
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

	@SuppressWarnings("unchecked")
	protected <T> ResourceList<T> toList(JsonApiResponse response) {
		Object entity = response.getEntity();
		List<T> list;
		if(entity instanceof List){
			list = (List<T>) entity;
		}else{
			list = (List<T>) Arrays.asList(entity);
		}
		LinksInformation linksInformation = response.getLinksInformation();
		MetaInformation metaInformation = response.getMetaInformation();
		return new ResourceList<>(list, linksInformation, metaInformation);
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
		String body = response.body().string(); // wise to do null check here

		ErrorResponse errorResponse = null;
		if(body.length() > 0){
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
