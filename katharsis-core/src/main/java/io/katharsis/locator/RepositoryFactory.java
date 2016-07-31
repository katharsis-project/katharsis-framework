package io.katharsis.locator;

/**
 * Central interface to provide domain repositories to the framework.
 * Each repository is fetched from implementation of this factory.
 */
public interface RepositoryFactory {

    /**
     * Get an instance of a class
     *
     * @param clazz class to be searched for
     * @param <T>   type of returning object
     * @return instance of a class of type T which implements/extends or is an instance of clazz
     */
    @Deprecated
    <T> T getInstance(Class<T> clazz);


    /**
     * Get an instance of a repository class. You may get a cached instance.
     *
     * @param clazz class to be searched for
     * @return instance of a class of type T which implements/extends or is an instance of clazz
     */
    Object build(Class clazz);

}
