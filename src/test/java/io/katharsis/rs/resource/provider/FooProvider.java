package io.katharsis.rs.resource.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.rs.provider.RequestContextParameterProvider;

import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Parameter;

public class FooProvider implements RequestContextParameterProvider<String> {

    @Override
    public String provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
        return "foo";
    }

    @Override
    public boolean provides(Parameter parameter) {
        return parameter.isAnnotationPresent(Foo.class);
    }
}
