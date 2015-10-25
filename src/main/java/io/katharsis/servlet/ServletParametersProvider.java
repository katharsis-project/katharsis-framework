package io.katharsis.servlet;

import io.katharsis.invoker.KatharsisInvokerContext;
import io.katharsis.repository.RepositoryMethodParameterProvider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public class ServletParametersProvider implements RepositoryMethodParameterProvider {

    private KatharsisInvokerContext katharsisInvokerContext;

    public ServletParametersProvider(KatharsisInvokerContext katharsisInvokerContext) {
        this.katharsisInvokerContext = katharsisInvokerContext;
    }

    @Override
    public <T> T provide(Parameter parameter) {
        Object returnValue = null;
        if (ServletContext.class.isAssignableFrom(parameter.getType())) {
            returnValue = katharsisInvokerContext.getServletContext();
        } else if (HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
            returnValue = katharsisInvokerContext.getServletRequest();
        } else if (HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
            returnValue = katharsisInvokerContext.getServletResponse();
        }
        return (T) returnValue;
    }
}
