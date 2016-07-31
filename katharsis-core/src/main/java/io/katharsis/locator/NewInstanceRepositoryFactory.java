package io.katharsis.locator;

import io.katharsis.dispatcher.registry.annotated.AnnotatedResourceRepositoryAdapter;
import io.katharsis.dispatcher.registry.annotated.ParametersFactory;
import lombok.Data;

/**
 * Sample implementation of {@link RepositoryFactory}. It makes new instance for every method call.
 */
@Data
public class NewInstanceRepositoryFactory implements RepositoryFactory {

    private final ParametersFactory parametersFactory;

    @Override
    public <T> T getInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AnnotatedResourceRepositoryAdapter build(Class clazz) {
        try {
            return new AnnotatedResourceRepositoryAdapter(clazz.newInstance(), parametersFactory);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
