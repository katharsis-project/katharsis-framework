package io.katharsis.brave;

import io.katharsis.client.http.okhttp.OkHttpAdapter;

public class OkHttpBraveModuleTest extends AbstractBraveModuleTest {

	public OkHttpBraveModuleTest() {
		super(OkHttpAdapter.newInstance());
	}
}
