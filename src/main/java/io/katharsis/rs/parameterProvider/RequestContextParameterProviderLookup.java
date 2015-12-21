package io.katharsis.rs.parameterProvider;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.resource.exception.init.InvalidResourceException;
import io.katharsis.rs.parameterProvider.provider.RequestContextParameterProvider;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class RequestContextParameterProviderLookup {

    private String resourceSearchPackage;
    private JsonServiceLocator jsonServiceLocator;

    public RequestContextParameterProviderLookup(String resourceSearchPackage, JsonServiceLocator jsonServiceLocator) {
        this.resourceSearchPackage = resourceSearchPackage;
        this.jsonServiceLocator = jsonServiceLocator;
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

        Set<RequestContextParameterProvider> providers = new HashSet<>(parameterProviderClasses.size());
        for (Class<? extends RequestContextParameterProvider> parameterProviderClazz : parameterProviderClasses) {
            try {
                providers.add(jsonServiceLocator.getInstance(parameterProviderClazz));
            } catch (Exception e) {
                throw new InvalidResourceException(parameterProviderClazz.getCanonicalName() + " can not be initialized", e);
            }
        }

        return providers;
    }
}
