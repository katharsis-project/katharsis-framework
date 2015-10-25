package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.repository.RepositoryMethodParameterProvider;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.lang.reflect.Parameter;

/**
 * <p>
 * An implementation of parameter provider for JAX-RS integration. It supports the following parameters:
 * </p>
 * <ol>
 *     <li>{@link ContainerRequestContext}</li>
 *     <li>{@link SecurityContext}</li>
 *     <li>Values annotated with {@link CookieParam}</li>
 *     <li>Values annotated with {@link HeaderParam}</li>
 * </ol>
 * <p>
 * Value casting for values annotated with {@link CookieParam} and {@link HeaderParam} does <b>not</b> conform with the
 * definitions described in the JAX-RS specification. If a value is not String or {@link Cookie} for
 * {@link CookieParam}, an instance of {@link ObjectMapper} is used to map the value to the desired type.
 * </p>
 */
public class JaxRsParameterProvider implements RepositoryMethodParameterProvider {

    private final ObjectMapper objectMapper;
    private final ContainerRequestContext requestContext;

    public JaxRsParameterProvider(ObjectMapper objectMapper, ContainerRequestContext requestContext) {
        this.objectMapper = objectMapper;
        this.requestContext = requestContext;
    }

    @Override
    public <T> T provide(Parameter parameter) {
        Object returnValue = null;
        if (ContainerRequestContext.class.isAssignableFrom(parameter.getType())) {
            returnValue = requestContext;
        } else if (SecurityContext.class.isAssignableFrom(parameter.getType())) {
            returnValue = requestContext.getSecurityContext();
        } else if (parameter.isAnnotationPresent(CookieParam.class)) {
            returnValue = extractCookie(parameter);
        } else if (parameter.isAnnotationPresent(HeaderParam.class)) {
            returnValue = extractHeader(parameter);
        }
        return (T) returnValue;
    }

    private Object extractHeader(Parameter parameter) {
        Object returnValue;
        String value = requestContext.getHeaderString(parameter.getAnnotation(HeaderParam.class).value());
        if (value == null) {
            return null;
        } else {
            if (String.class.isAssignableFrom(parameter.getType())) {
                returnValue = value;
            } else {
                try {
                    returnValue = objectMapper.readValue(value, parameter.getType());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return returnValue;
    }

    private Object extractCookie(Parameter parameter) {
        Object returnValue;
        String cookieName = parameter.getAnnotation(CookieParam.class).value();
        Cookie cookie = requestContext.getCookies().get(cookieName);
        if (cookie == null) {
            return null;
        } else {
            if (Cookie.class.isAssignableFrom(parameter.getType())) {
                returnValue = cookie;
            } else if (String.class.isAssignableFrom(parameter.getType())) {
                returnValue = cookie.getValue();
            } else {
                try {
                    returnValue = objectMapper.readValue(cookie.getValue(), parameter.getType());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return returnValue;
    }
}
