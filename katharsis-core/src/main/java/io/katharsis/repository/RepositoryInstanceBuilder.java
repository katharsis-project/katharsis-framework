package io.katharsis.repository;

import io.katharsis.locator.RepositoryFactory;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;

/**
 * This builder is responsible for creating a new instance of a repository
 * <p/>
 * Use Repository Factory instead of this class.
 */
@Deprecated
public class RepositoryInstanceBuilder<T> {

    private final RepositoryFactory repositoryFactory;
    private final Class<T> repositoryClass;

    public RepositoryInstanceBuilder(RepositoryFactory repositoryFactory, Class<T> repositoryClass) {
        this.repositoryFactory = repositoryFactory;
        this.repositoryClass = repositoryClass;
    }

    public T buildRepository() {
        T repoInstance = repositoryFactory.getInstance(repositoryClass);
        if (repoInstance == null) {
            throw new RepositoryInstanceNotFoundException(repositoryClass.getCanonicalName());
        }
        return repoInstance;
    }

    public Class<T> getRepositoryClass() {
        return repositoryClass;
    }
}
