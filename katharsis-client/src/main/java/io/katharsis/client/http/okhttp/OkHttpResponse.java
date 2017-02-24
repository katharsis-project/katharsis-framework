package io.katharsis.client.http.okhttp;

import java.io.IOException;

import io.katharsis.client.http.HttpAdapterResponse;
import okhttp3.Response;

public class OkHttpResponse implements HttpAdapterResponse {

	private Response response;

	public OkHttpResponse(Response response) {
		this.response = response;
	}

	@Override
	public boolean isSuccessful() {
		return response.isSuccessful();
	}

	@Override
	public String body() throws IOException {
		return response.body().string();
	}

	@Override
	public int code() {
		return response.code();
	}

	@Override
	public String message() {
		return response.message();
	}

	@Override
	public String getResponseHeader(String name) {
		return response.header(name);
	}

}
