package io.katharsis.example.wildfly.serviceLocator;

import io.katharsis.locator.JsonServiceLocator;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class WildflyServiceLocator implements JsonServiceLocator {

    @Override
    public <T> T getInstance(Class<T> aClass) {
        BeanManager beanManager = CDI.current().getBeanManager();
        Bean bean = beanManager.resolve(beanManager.getBeans(aClass));
        CreationalContext creationalContext = beanManager.createCreationalContext(bean);

        return (T) beanManager.getReference(bean, aClass, creationalContext);
    }
}
