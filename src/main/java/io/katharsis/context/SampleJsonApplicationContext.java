package io.katharsis.context;

/**
 * Sample implementation of JsonApplicationContext. It makes new instances for every method call.
 */
public class SampleJsonApplicationContext implements JsonApplicationContext {
    @Override
    public <T> T getInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
