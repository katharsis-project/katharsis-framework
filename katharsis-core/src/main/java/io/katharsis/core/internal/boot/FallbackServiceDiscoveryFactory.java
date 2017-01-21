package io.katharsis.core.internal.boot;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.module.ServiceDiscovery;
import io.katharsis.module.ServiceDiscoveryFactory;

public class FallbackServiceDiscoveryFactory implements ServiceDiscoveryFactory {

	ServiceDiscoveryFactory factory;

	JsonServiceLocator serviceLocator;

	PropertiesProvider propertiesProvider;

	public FallbackServiceDiscoveryFactory(ServiceDiscoveryFactory factory, JsonServiceLocator serviceLocator,
			PropertiesProvider propertiesProvider) {
		this.factory = factory;
		this.serviceLocator = serviceLocator;
		this.propertiesProvider = propertiesProvider;
	}

	@Override
	public ServiceDiscovery getInstance() {
		ServiceDiscovery instance = factory.getInstance();
		if (instance != null) {
			return instance;
		}
		String resourceSearchPackage = propertiesProvider.getProperty(KatharsisBootProperties.RESOURCE_SEARCH_PACKAGE);
		return new ReflectionsServiceDiscovery(resourceSearchPackage, serviceLocator);
	}

}
