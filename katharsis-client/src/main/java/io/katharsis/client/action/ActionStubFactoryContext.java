package io.katharsis.client.action;

import io.katharsis.client.http.HttpAdapter;
import io.katharsis.resource.registry.ServiceUrlProvider;

public interface ActionStubFactoryContext {

	ServiceUrlProvider getServiceUrlProvider();

	HttpAdapter getHttpAdapter();

}
