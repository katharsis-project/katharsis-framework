package io.katharsis.client;

import io.katharsis.client.http.apache.HttpClientAdapter;

public class ApacheHttpClientTest extends QuerySpecClientTest {

	@Override
	protected void setupClient(KatharsisClient client) {
		super.setupClient(client);
		client.setHttpAdapter(HttpClientAdapter.newInstance());
	}
}
