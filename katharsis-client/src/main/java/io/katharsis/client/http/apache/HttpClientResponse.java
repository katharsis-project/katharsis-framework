package io.katharsis.client.http.apache;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import io.katharsis.client.http.HttpAdapterResponse;

public class HttpClientResponse implements HttpAdapterResponse {

	private CloseableHttpResponse response;

	private String body;

	public HttpClientResponse(CloseableHttpResponse response) throws ParseException, IOException {
		this.response = response;

		HttpEntity entity = response.getEntity();
		if (entity != null) {
			body = EntityUtils.toString(entity);
		}
	}

	@Override
	public boolean isSuccessful() {
		return response.getStatusLine().getStatusCode() < 400;
	}

	@Override
	public String body() throws IOException {
		return body;
	}

	@Override
	public int code() {
		return response.getStatusLine().getStatusCode();
	}

	@Override
	public String message() {
		return response.getStatusLine().getReasonPhrase();
	}

	@Override
	public String getResponseHeader(String name) {
		Header header = response.getFirstHeader(name);
		return header != null ? header.getValue() : null;
	}

}
