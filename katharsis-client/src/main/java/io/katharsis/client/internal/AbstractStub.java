package io.katharsis.client.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.ClientException;
import io.katharsis.client.KatharsisClient;
import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.HttpAdapterRequest;
import io.katharsis.client.http.HttpAdapterResponse;
import io.katharsis.client.internal.core.BaseResponseContext;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.utils.JsonApiUrlBuilder;
import io.katharsis.utils.java.Optional;

public class AbstractStub {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStub.class);

	public static final String CONTENT_TYPE = "application/vnd.api+json";

	protected KatharsisClient katharsis;

	protected JsonApiUrlBuilder urlBuilder;

	public AbstractStub(KatharsisClient client, JsonApiUrlBuilder urlBuilder) {
		this.katharsis = client;
		this.urlBuilder = urlBuilder;
	}

	protected BaseResponseContext executeGet(String requestUrl) {
		return execute(requestUrl, true, HttpMethod.GET, null);
	}

	protected BaseResponseContext executeDelete(String requestUrl) {
		return execute(requestUrl, false, HttpMethod.DELETE, null);
	}

	@SuppressWarnings("unchecked")
	protected <T> DefaultResourceList<T> toList(JsonApiResponse response) {
		Object entity = response.getEntity();
		List<T> list;
		if (entity instanceof List) {
			list = (List<T>) entity;
		}
		else {
			list = (List<T>) Arrays.asList(entity);
		}
		LinksInformation linksInformation = response.getLinksInformation();
		MetaInformation metaInformation = response.getMetaInformation();
		return new DefaultResourceList<T>(list, metaInformation, linksInformation);
	}

	protected BaseResponseContext execute(String url, boolean getResponse, HttpMethod method, String requestBody) {
		try {


			HttpAdapter httpAdapter = katharsis.getHttpAdapter();
			HttpAdapterRequest request = httpAdapter.newRequest(url, method, requestBody);
			
			LOGGER.debug("requesting {} {}", method, url);
			if(requestBody != null){
				LOGGER.debug("request body: {}", requestBody);
			}

			request.header("Content-Type", CONTENT_TYPE);
			request.header("Accept", CONTENT_TYPE);

			HttpAdapterResponse response = request.execute();
			if (!response.isSuccessful()) {
				handleError(response);
			}

			String body = response.body();
			LOGGER.debug("response body: {}", body);
			ObjectMapper objectMapper = katharsis.getObjectMapper();
			if (getResponse) {
				return objectMapper.readValue(body, BaseResponseContext.class);
			}
			else {
				return null;
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void handleError(HttpAdapterResponse response) throws IOException {
		String body = response.body();

		ErrorResponse errorResponse = null;
		if (body.length() > 0) {
			ObjectMapper objectMapper = katharsis.getObjectMapper();
			errorResponse = objectMapper.readValue(body, ErrorResponse.class);
		}
		if (errorResponse != null) {
			errorResponse = new ErrorResponse((Iterable<ErrorData>) errorResponse.getResponse().getEntity(), response.code());
		}
		else {
			errorResponse = new ErrorResponse(null, response.code());
		}

		ExceptionMapperRegistry exceptionMapperRegistry = katharsis.getExceptionMapperRegistry();
		Optional<ExceptionMapper<?>> mapper = exceptionMapperRegistry.findMapperFor(errorResponse);
		if (mapper.isPresent()) {
			Throwable throwable = mapper.get().fromErrorResponse(errorResponse);
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			}
			else {
				throw new ClientException(response.code(), response.message(), throwable);
			}
		}
		else {
			throw new ClientException(response.code(), response.message());
		}
	}
}
