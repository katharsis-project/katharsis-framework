package io.katharsis.client.http.okhttp;

import okhttp3.OkHttpClient.Builder;

public interface OkHttpAdapterListener {

	void onBuild(Builder builder);

}
