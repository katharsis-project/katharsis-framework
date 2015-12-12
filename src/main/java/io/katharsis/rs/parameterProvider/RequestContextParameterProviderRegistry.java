package io.katharsis.rs.parameterProvider;

import io.katharsis.rs.parameterProvider.provider.RequestContextParameterProvider;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class RequestContextParameterProviderRegistry {

    private Set<RequestContextParameterProvider> parameterProviders;

    public RequestContextParameterProviderRegistry(Set<RequestContextParameterProvider> parameterProviders) {
        this.parameterProviders = parameterProviders;
    }

    public Optional<RequestContextParameterProvider> findProviderFor(Parameter parameter) {
        for (RequestContextParameterProvider parameterProvider : parameterProviders) {
            if (parameterProvider.provides(parameter)) {
                return Optional.of(parameterProvider);
            }
        }
        return Optional.empty();
    }

    public Collection<RequestContextParameterProvider> getParameterProviders() {
        return parameterProviders;
    }

    public void setParameterProviders(Set<RequestContextParameterProvider> parameterProviders) {
        this.parameterProviders = parameterProviders;
    }
}
