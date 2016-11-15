package io.katharsis.client.http;

import java.util.concurrent.TimeUnit;

import io.katharsis.dispatcher.controller.HttpMethod;

public interface HttpAdapter {

	HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody);

	void setReceiveTimeout(int timeout, TimeUnit unit);

}
