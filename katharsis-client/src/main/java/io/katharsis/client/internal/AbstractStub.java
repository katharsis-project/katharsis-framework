package io.katharsis.client.internal;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.ClientException;
import io.katharsis.client.KatharsisClient;
import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.HttpAdapterRequest;
import io.katharsis.client.http.HttpAdapterResponse;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.utils.JsonApiUrlBuilder;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.resource.Document;
import io.katharsis.utils.Optional;

public class AbstractStub {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStub.class);

	public static final String CONTENT_TYPE = "application/vnd.api+json";

	protected KatharsisClient client;

	protected JsonApiUrlBuilder urlBuilder;

	public AbstractStub(KatharsisClient client, JsonApiUrlBuilder urlBuilder) {
		this.client = client;
		this.urlBuilder = urlBuilder;
	}

	protected Object executeGet(String requestUrl, ResponseType responseType) {
		return execute(requestUrl, responseType, HttpMethod.GET, null);
	}

	protected Object executeDelete(String requestUrl) {
		return execute(requestUrl, ResponseType.NONE, HttpMethod.DELETE, null);
	}

	public enum ResponseType {
		NONE, RESOURCE, RESOURCES
	}

	protected Object execute(String url, ResponseType responseType, HttpMethod method, String requestBody) {
		try {

			HttpAdapter httpAdapter = client.getHttpAdapter();
			HttpAdapterRequest request = httpAdapter.newRequest(url, method, requestBody);

			LOGGER.debug("requesting {} {}", method, url);
			if (requestBody != null) {
				LOGGER.debug("request body: {}", requestBody);
			}

			request.header("Content-Type", CONTENT_TYPE);
			request.header("Accept", CONTENT_TYPE);

			HttpAdapterResponse response = request.execute();

			if (!response.isSuccessful()) {
				throw handleError(response);
			}

			String body = response.body();
			LOGGER.debug("response body: {}", body);
			ObjectMapper objectMapper = client.getObjectMapper();

			if (responseType != ResponseType.NONE) {
				Document document = objectMapper.readValue(body, Document.class);

				ClientDocumentMapper documentMapper = client.getDocumentMapper();
				return documentMapper.fromDocument(document, responseType == ResponseType.RESOURCES);
			}
			return null;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private RuntimeException handleError(HttpAdapterResponse response) throws IOException {
		ErrorResponse errorResponse = null;
		String body = response.body();
		String contentType = response.getResponseHeader("content-type");
		if (body.length() > 0 && CONTENT_TYPE.equalsIgnoreCase(contentType)) {

			ObjectMapper objectMapper = client.getObjectMapper();
			Document document = objectMapper.readValue(body, Document.class);
			if (document.getErrors() != null && !document.getErrors().isEmpty()) {
				errorResponse = new ErrorResponse(document.getErrors(), response.code());
			}
		}
		if (errorResponse == null) {
			errorResponse = new ErrorResponse(null, response.code());
		}

		ExceptionMapperRegistry exceptionMapperRegistry = client.getExceptionMapperRegistry();
		Optional<ExceptionMapper<?>> mapper = exceptionMapperRegistry.findMapperFor(errorResponse);
		if (mapper.isPresent()) {
			Throwable throwable = mapper.get().fromErrorResponse(errorResponse);
			if (throwable instanceof RuntimeException) {
				return (RuntimeException) throwable;
			} else {
				return new ClientException(response.code(), response.message(), throwable);
			}
		} else {
			return new ClientException(response.code(), response.message());
		}
	}
}
