package io.katharsis.rs.parameterProvider;

import io.katharsis.rs.parameterProvider.provider.*;

import java.util.HashSet;
import java.util.Set;

public class RequestContextParameterProviderRegistryBuilder {

    private Set<RequestContextParameterProvider> providers = new HashSet<>();

    public RequestContextParameterProviderRegistry build(RequestContextParameterProviderLookup containerRequestContextProviderLookup) {
        addKatharsisDefaultProviders();
        for (RequestContextParameterProvider parameterProvider : containerRequestContextProviderLookup.getRequestContextProviders()) {
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
