package io.katharsis.example.springboot.simple.filter;

import io.katharsis.invoker.KatharsisInvokerBuilder;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.servlet.SampleKatharsisFilter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.StringUtils;

/**
 * Simple Spring Boot enabled example KatharsisFilter implementation.
 * <P>
 * This filter simply extends {@link SampleKatharsisFilter} and overrides {@link #createKatharsisInvokerBuilder()}
 * in order to register a custom {@link JsonServiceLocator} component.
 * </P>
 * <P>
 * The custom {@link JsonServiceLocator} component created here simply retrieve a bean component
 * by the repository class type from the underlying {@link BeanFactory}.
 * </P>
 */
public class SpringBootSampleKatharsisFilter extends SampleKatharsisFilter implements BeanFactoryAware {

    private static final String DEFAULT_RESOURCE_SEARCH_PACKAGE = "io.katharsis.example.springboot.simple.domain";

    private static final String RESOURCE_DEFAULT_DOMAIN = "http://localhost:8080";

    private BeanFactory beanFactory;

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
        this.beanFactory = beanFactory;
    }

    @Override
    protected KatharsisInvokerBuilder createKatharsisInvokerBuilder() {
        KatharsisInvokerBuilder builder = new KatharsisInvokerBuilder();;

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

}
