package io.katharsis.rs.resource.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.rs.parameterProvider.provider.RequestContextParameterProvider;

import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Parameter;

public class AuthRequestProvider implements RequestContextParameterProvider<AuthRequest> {
    @Override
    public AuthRequest provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
        return AuthRequest.fromAuthorizationHeader(requestContext.getHeaderString("Authorization"));
    }

    @Override
    public boolean provides(Parameter parameter) {
        return AuthRequest.class.isAssignableFrom(parameter.getType());
    }
}
