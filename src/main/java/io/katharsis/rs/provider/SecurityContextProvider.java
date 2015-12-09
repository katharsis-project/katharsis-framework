package io.katharsis.rs.provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Parameter;

public class SecurityContextProvider implements RequestContextParameterProvider<SecurityContext> {

    @Override
    public SecurityContext provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
        return requestContext.getSecurityContext();
    }

    @Override
    public boolean provides(Parameter parameter) {
        return SecurityContext.class.isAssignableFrom(parameter.getType());
    }

}
