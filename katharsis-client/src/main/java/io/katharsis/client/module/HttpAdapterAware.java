package io.katharsis.client.module;

import io.katharsis.client.http.HttpAdapter;

/**
 * Can be implemented by modules to get access to the HttpAdapter implementation. 
 */
public interface HttpAdapterAware {

	public void setHttpAdapter(HttpAdapter client);
}
