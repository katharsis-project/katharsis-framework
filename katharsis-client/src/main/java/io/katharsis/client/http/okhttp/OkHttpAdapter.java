package io.katharsis.client.http.okhttp;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import io.katharsis.client.http.HttpAdapter;
import io.katharsis.client.http.HttpAdapterRequest;
import io.katharsis.dispatcher.controller.HttpMethod;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public class OkHttpAdapter implements HttpAdapter {

	private OkHttpClient impl;

	private CopyOnWriteArrayList<OkHttpAdapterListener> listeners = new CopyOnWriteArrayList<>();

	private Long networkTimeout;

	public void addListener(OkHttpAdapterListener listener) {
		if (impl != null) {
			throw new IllegalStateException("already initialized");
		}
		listeners.add(listener);
	}

	public OkHttpClient getImplementation() {
		if (impl == null) {
			initImpl();
		}
		return impl;
	}

	private void initImpl() {
		synchronized (this) {
			if (impl == null) {
				Builder builder = new OkHttpClient.Builder();

				if (networkTimeout != null) {
					builder.readTimeout(networkTimeout, TimeUnit.MILLISECONDS);
				}

				for (OkHttpAdapterListener listener : listeners) {
					listener.onBuild(builder);
				}
				impl = builder.build();
			}
		}
	}

	@Override
	public HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody) {
		OkHttpClient impl = getImplementation();
		return new OkHttpRequest(impl, url, method, requestBody);
	}

	public static HttpAdapter newInstance() {
		return new OkHttpAdapter();
	}

	@Override
	public void setReceiveTimeout(int timeout, TimeUnit unit) {
		networkTimeout = unit.toMillis(timeout);
	}
}
