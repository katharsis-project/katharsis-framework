package io.katharsis.legacy.registry;

import io.katharsis.errorhandling.exception.RepositoryInstanceNotFoundException;
import io.katharsis.legacy.locator.JsonServiceLocator;

/**
 * This builder is responsible for creating a new instance of a repository
 */
public class RepositoryInstanceBuilder<T> {

    private final JsonServiceLocator jsonServiceLocator;
    private final Class<T> repositoryClass;

    public RepositoryInstanceBuilder(JsonServiceLocator jsonServiceLocator, Class<T> repositoryClass) {
        this.jsonServiceLocator = jsonServiceLocator;
        this.repositoryClass = repositoryClass;
    }

    public T buildRepository() {
        T repoInstance = jsonServiceLocator.getInstance(repositoryClass);
        if (repoInstance == null) {
            throw new RepositoryInstanceNotFoundException(repositoryClass.getCanonicalName());
        }
        return repoInstance;
    }

    public Class<T> getRepositoryClass() {
        return repositoryClass;
    }
}
