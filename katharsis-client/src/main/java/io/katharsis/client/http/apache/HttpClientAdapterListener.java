package io.katharsis.client.http.apache;

import org.apache.http.impl.client.HttpClientBuilder;

public interface HttpClientAdapterListener {

	void onBuild(HttpClientBuilder builder);

}
