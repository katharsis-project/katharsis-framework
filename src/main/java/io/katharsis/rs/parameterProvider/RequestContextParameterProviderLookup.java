package io.katharsis.rs.parameterProvider;

import io.katharsis.resource.exception.init.InvalidResourceException;
import io.katharsis.rs.parameterProvider.provider.RequestContextParameterProvider;
import org.reflections.Reflections;

import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class RequestContextParameterProviderLookup {

    private String resourceSearchPackage;

    public RequestContextParameterProviderLookup(String resourceSearchPackage) {
        this.resourceSearchPackage = resourceSearchPackage;
    }

    public Set<RequestContextParameterProvider> getRequestContextProviders() {
        Reflections reflections;
        if(this.resourceSearchPackage != null) {
            String[] exceptionMapperClasses = this.resourceSearchPackage.split(",");
            reflections = new Reflections(exceptionMapperClasses);
        } else {
            reflections = new Reflections(this.resourceSearchPackage, new Scanner[0]);
        }

        Set<Class<? extends RequestContextParameterProvider>> parameterProviderClasses = reflections.getSubTypesOf(RequestContextParameterProvider.class);

        return parameterProviderClasses.stream().map((parameterProviderClazz) -> {
            try {
                return parameterProviderClazz.newInstance();
            } catch (Exception e) {
                throw new InvalidResourceException(parameterProviderClazz.getCanonicalName() + " can not be initialized", e);
            }
        }).collect(Collectors.<RequestContextParameterProvider>toSet());
    }
}
