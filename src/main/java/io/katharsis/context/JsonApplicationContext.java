package io.katharsis.context;

/**
 * Central interface to provide objects to the framework. Each repository and other framework services are fetched
 * from implementation of this context.
 */
public interface JsonApplicationContext {

    /**
     * Get an instance of a class
     * @param clazz class to be searched for
     * @param <T> type of returning object
     * @return instance of a class of type T which implements/extends or is instance of clazz
     */
    <T> T getInstance(Class<T> clazz);
}
