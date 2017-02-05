package io.katharsis.core.internal.boot;

import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.legacy.locator.JsonServiceLocator;
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
		String resourceSearchPackage = propertiesProvider.getProperty(KatharsisProperties.RESOURCE_SEARCH_PACKAGE);
		return new ReflectionsServiceDiscovery(resourceSearchPackage, serviceLocator);
	}

}
