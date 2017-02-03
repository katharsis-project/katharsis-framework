package io.katharsis.client.http.okhttp;

import java.io.IOException;

import io.katharsis.client.http.HttpAdapterRequest;
import io.katharsis.client.http.HttpAdapterResponse;
import io.katharsis.repository.request.HttpMethod;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpRequest implements HttpAdapterRequest {

	private Builder builder;

	private OkHttpClient client;

	public OkHttpRequest(OkHttpClient client, String url, HttpMethod method, String requestBody) {
		this.client = client;
		builder = new Request.Builder().url(url);
		
		RequestBody requestBodyObj = requestBody != null ? RequestBody.create(null, requestBody) : null;
		builder.method(method.toString(), requestBodyObj);
	}

	@Override
	public void header(String name, String value) {
		builder = builder.header(name, value);
	}

	@Override
	public HttpAdapterResponse execute() throws IOException {
		Request request = builder.build();
		Response response = client.newCall(request).execute();
		return new OkHttpResponse(response);
	}

}
