package io.katharsis.brave;

import io.katharsis.client.http.apache.HttpClientAdapter;

public class ApacheHttpBraveModuleTest extends AbstractBraveModuleTest {

	public ApacheHttpBraveModuleTest() {
		super(HttpClientAdapter.newInstance());
	}
}
