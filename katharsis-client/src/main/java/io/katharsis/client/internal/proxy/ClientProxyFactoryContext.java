package io.katharsis.client.internal.proxy;

import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.list.DefaultResourceList;

public interface ClientProxyFactoryContext {

	ModuleRegistry getModuleRegistry();

	<T> DefaultResourceList<T> getCollection(Class<T> resourceClass, String url);

}
