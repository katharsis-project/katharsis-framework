package io.katharsis.rs.provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Parameter;

public class ContainerRequestContextProvider implements RequestContextParameterProvider<ContainerRequestContext> {

    @Override
    public ContainerRequestContext provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
        return requestContext;
    }

    @Override
    public boolean provides(Parameter parameter) {
        return ContainerRequestContext.class.isAssignableFrom(parameter.getType());
    }
}
