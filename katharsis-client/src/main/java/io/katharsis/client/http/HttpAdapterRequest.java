package io.katharsis.client.http;

import java.io.IOException;

public interface HttpAdapterRequest {

	void header(String name, String value);

	HttpAdapterResponse execute() throws IOException;

}
