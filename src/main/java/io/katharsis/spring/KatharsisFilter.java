package io.katharsis.spring;

import io.katharsis.invoker.KatharsisInvokerBuilder;
import io.katharsis.invoker.KatharsisInvokerContext;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.servlet.SampleKatharsisFilter;
import io.katharsis.servlet.ServletKatharsisInvokerContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class KatharsisFilter extends SampleKatharsisFilter implements BeanFactoryAware {

    private static final String DEFAULT_RESOURCE_SEARCH_PACKAGE = null;

    private static final String RESOURCE_DEFAULT_DOMAIN = "http://localhost:8080";

    private ConfigurableBeanFactory beanFactory;

    @Override
    public String getResourceSearchPackage() {
        String resourceSearchPackage = super.getResourceSearchPackage();

        if (StringUtils.isEmpty(resourceSearchPackage)) {
            resourceSearchPackage = DEFAULT_RESOURCE_SEARCH_PACKAGE;
        }

        return resourceSearchPackage;
    }

    @Override
    public String getResourceDefaultDomain() {
        String resourceDefaultDomain = super.getResourceDefaultDomain();

        if (StringUtils.isEmpty(resourceDefaultDomain)) {
            resourceDefaultDomain = RESOURCE_DEFAULT_DOMAIN;
        }

        return resourceDefaultDomain;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    @Override
    protected KatharsisInvokerBuilder createKatharsisInvokerBuilder() {
        KatharsisInvokerBuilder builder = new KatharsisInvokerBuilder();

        builder.resourceSearchPackage(getResourceSearchPackage())
            .resourceDefaultDomain(getResourceDefaultDomain())
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
                String filterBasePath = getFilterBasePath();

                if (filterBasePath != null && path.startsWith(filterBasePath)) {
                    path = path.substring(filterBasePath.length());
                }

                return path;
            }

            @Override
            public RepositoryMethodParameterProvider getParameterProvider() {
                return new SpringParameterProvider(beanFactory);
            }
        };
    }
}
