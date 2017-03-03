package io.katharsis.rs.internal.parameterProvider;

import java.util.HashSet;
import java.util.Set;

import io.katharsis.module.ServiceDiscovery;
import io.katharsis.rs.internal.parameterProvider.provider.ContainerRequestContextProvider;
import io.katharsis.rs.internal.parameterProvider.provider.CookieParamProvider;
import io.katharsis.rs.internal.parameterProvider.provider.HeaderParamProvider;
import io.katharsis.rs.internal.parameterProvider.provider.QueryParamProvider;
import io.katharsis.rs.internal.parameterProvider.provider.RequestContextParameterProvider;
import io.katharsis.rs.internal.parameterProvider.provider.SecurityContextProvider;

public class RequestContextParameterProviderRegistryBuilder {

	private Set<RequestContextParameterProvider> providers = new HashSet<>();

	public RequestContextParameterProviderRegistry build(ServiceDiscovery discovery) {
		addKatharsisDefaultProviders();
		for (RequestContextParameterProvider parameterProvider : discovery.getInstancesByType(RequestContextParameterProvider.class)) {
			registerRequestContextProvider(parameterProvider);
		}
		return new RequestContextParameterProviderRegistry(providers);
	}

	private void addKatharsisDefaultProviders() {
		registerRequestContextProvider(new ContainerRequestContextProvider());
		registerRequestContextProvider(new SecurityContextProvider());
		registerRequestContextProvider(new CookieParamProvider());
		registerRequestContextProvider(new HeaderParamProvider());
		registerRequestContextProvider(new QueryParamProvider());
	}

	private void registerRequestContextProvider(RequestContextParameterProvider requestContextParameterProvider) {
		providers.add(requestContextParameterProvider);
	}
}
