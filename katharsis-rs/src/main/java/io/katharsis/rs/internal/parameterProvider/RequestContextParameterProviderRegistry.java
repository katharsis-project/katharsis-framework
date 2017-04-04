package io.katharsis.rs.internal.parameterProvider;

import io.katharsis.rs.internal.parameterProvider.provider.Parameter;
import io.katharsis.rs.internal.parameterProvider.provider.RequestContextParameterProvider;
import io.katharsis.utils.Optional;

import java.util.Collection;
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
