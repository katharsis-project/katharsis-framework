package io.katharsis.core.internal.boot;

import java.util.Iterator;
import java.util.ServiceLoader;

import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.module.ServiceDiscovery;
import io.katharsis.module.ServiceDiscoveryFactory;

/**
 * Searches for an implementation of the ServiceDiscovery with java.util.ServiceLoader. Add e.g. katharsis-cdi to your classpath
 * to pickup the CdiServiceDiscovery.
 */
public class DefaultServiceDiscoveryFactory implements ServiceDiscoveryFactory {

	@Override
	public ServiceDiscovery getInstance() {
		ServiceLoader<ServiceDiscovery> loader = ServiceLoader.load(ServiceDiscovery.class);
		Iterator<ServiceDiscovery> iterator = loader.iterator();
		if (iterator.hasNext()) {
			ServiceDiscovery discovery = iterator.next();
			PreconditionUtil.assertFalse("expected unique ServiceDiscovery implementation, got: " + loader, iterator.hasNext());
			return discovery;
		}
		return null;
	}
}
