package io.katharsis.spring;

import io.katharsis.repository.RepositoryMethodParameterProvider;
import org.springframework.beans.factory.BeanFactory;

public class SpringParameterProvider implements RepositoryMethodParameterProvider {
    private BeanFactory beanFactory;

    public SpringParameterProvider(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public <T> T provide(java.lang.reflect.Method method, int i) {
        return null;
    }
}
