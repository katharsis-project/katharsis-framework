package io.katharsis.client.internal;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;

import io.katharsis.client.KatharsisClient;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.response.BaseResponseContext;

public class AbstractStub {

	// TODO katharsis constant
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
			builder = builder.header("Content-Type", CONTENT_TYPE);

			Request request = builder.build();
			Response response = katharsis.getHttpClient().newCall(request).execute();
			if (!response.isSuccessful()) {
				// TODO proper exception handling

				// int code = response.code();
				// if(code == HttpStatus.NOT_FOUND_404)

				throw new ResourceException(response.message());
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
}
