package io.katharsis.client;

import io.katharsis.client.http.okhttp.OkHttpAdapter;

public class OkHttpClientTest extends QuerySpecClientTest {

	@Override
	protected void setupClient(KatharsisClient client) {
		super.setupClient(client);
		client.setHttpAdapter(OkHttpAdapter.newInstance());
	}
}
