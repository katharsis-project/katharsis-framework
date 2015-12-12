package io.katharsis.rs.parameterProvider.provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Parameter;

public interface RequestContextParameterProvider<T>  {

    public <T> T provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper);

    public boolean provides(Parameter parameter);
}
