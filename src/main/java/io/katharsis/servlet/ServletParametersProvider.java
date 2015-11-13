package io.katharsis.servlet;

import io.katharsis.repository.RepositoryMethodParameterProvider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ServletParametersProvider implements RepositoryMethodParameterProvider {

    private ServletContext servletContext;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;

    public ServletParametersProvider(ServletContext servletContext, HttpServletRequest httpServletRequest,
                                     HttpServletResponse httpServletResponse) {
        this.servletContext = servletContext;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public <T> T provide(Method method, int parameterIndex) {
        Parameter parameter = getParameter(method, parameterIndex);
        Object returnValue = null;
        if (ServletContext.class.isAssignableFrom(parameter.getType())) {
            returnValue = servletContext;
        } else if (HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
            returnValue = httpServletRequest;
        } else if (HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
            returnValue = httpServletResponse;
        }
        return (T) returnValue;
    }
}
