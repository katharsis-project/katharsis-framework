package io.katharsis.client.http.okhttp;

import java.util.concurrent.CopyOnWriteArrayList;

import io.katharsis.client.http.HttpAdapter;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public class OkHttpAdapter implements HttpAdapter {

	private OkHttpClient impl;

	private CopyOnWriteArrayList<OkHttpAdapterListener> listeners = new CopyOnWriteArrayList<>();

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
				for (OkHttpAdapterListener listener : listeners) {
					listener.onBuild(builder);
				}
				impl = builder.build();
			}
		}
	}
}
