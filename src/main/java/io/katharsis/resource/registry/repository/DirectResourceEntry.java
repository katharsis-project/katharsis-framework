package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.ResourceRepository;

import java.io.Serializable;

public class DirectResourceEntry<T, ID extends Serializable> implements ResourceEntry<T, ID> {
    private final RepositoryInstanceBuilder<ResourceRepository<T, ID>> repositoryInstanceBuilder;

    public DirectResourceEntry(RepositoryInstanceBuilder<ResourceRepository<T, ID>> repositoryInstanceBuilder) {
        this.repositoryInstanceBuilder = repositoryInstanceBuilder;
    }

    public ResourceRepository<T, ?> getResourceRepository() {
        return repositoryInstanceBuilder.buildRepository();
    }

    @Override
    public String toString() {
        return "DirectResourceEntry{" +
            "repositoryInstanceBuilder=" + repositoryInstanceBuilder +
            '}';
    }
}
