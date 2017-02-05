package io.katharsis.spring.legacy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import io.katharsis.invoker.internal.KatharsisInvokerContext;
import io.katharsis.invoker.internal.legacy.KatharsisInvokerBuilder;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.servlet.internal.ServletKatharsisInvokerContext;
import io.katharsis.servlet.legacy.SampleKatharsisFilter;
import io.katharsis.spring.SpringParameterProvider;

/**
 * Simple Spring enabled example KatharsisFilter implementation.
 * <p>
 * This filter simply extends {@link SampleKatharsisFilter} and overrides {@link #createKatharsisInvokerBuilder()}
 * in order to register a custom {@link JsonServiceLocator} component.
 * </P>
 * <p>
 * The custom {@link JsonServiceLocator} component created here simply retrieve a bean component
 * by the repository class type from the underlying {@link BeanFactory}.
 * </P>
 */
@Deprecated
public class KatharsisFilter extends SampleKatharsisFilter implements BeanFactoryAware {

    private String resourceSearchPackage;
    private String resourceDomain;
    private String pathPrefix;
    private ConfigurableBeanFactory beanFactory;

    @Autowired
    private KatharsisInvokerBuilder builder;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    @Override
    protected KatharsisInvokerBuilder createKatharsisInvokerBuilder() {
        builder.resourceSearchPackage(resourceSearchPackage)
            .resourceDefaultDomain(resourceDomain)
            .jsonServiceLocator(new JsonServiceLocator() {

                @Override
                public <T> T getInstance(Class<T> clazz) {
                    // Simply retrieve a bean by the repository class type.
                    return beanFactory.getBean(clazz);
                }

            });

        return builder;
    }

    @Override
    protected KatharsisInvokerContext createKatharsisInvokerContext(HttpServletRequest request, HttpServletResponse response) {
        return new ServletKatharsisInvokerContext(getServletContext(), request, response) {
            @Override
            public String getRequestPath() {
                String path = super.getRequestPath();

                if (pathPrefix != null && path.startsWith(pathPrefix)) {
                    path = path.substring(pathPrefix.length());
                }

                return path;
            }

            @Override
            public RepositoryMethodParameterProvider getParameterProvider() {
                return new SpringParameterProvider(beanFactory, getServletRequest());
            }
        };
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Override
    public void setResourceSearchPackage(String resourceSearchPackage) {
        this.resourceSearchPackage = resourceSearchPackage;
    }

    public void setResourceDomain(String resourceDomain) {
        this.resourceDomain = resourceDomain;
    }
}
