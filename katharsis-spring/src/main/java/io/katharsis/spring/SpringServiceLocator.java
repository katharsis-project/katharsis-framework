package io.katharsis.spring;

import io.katharsis.locator.RepositoryFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

public class SpringServiceLocator implements BeanFactoryAware, RepositoryFactory {

    private ConfigurableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return beanFactory.getBean(clazz);
    }

    @Override
    public Object build(Class clazz) {
        return beanFactory.getBean(clazz);
    }
}
