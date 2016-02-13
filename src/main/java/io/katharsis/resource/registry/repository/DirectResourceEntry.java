package io.katharsis.resource.registry.repository;

import io.katharsis.repository.ResourceRepository;

import java.io.Serializable;

public class DirectResourceEntry<T, ID extends Serializable> implements ResourceEntry<T, ID> {
    private final ResourceRepository<T, ID> resourceRepository;

    public DirectResourceEntry(ResourceRepository<T, ID> resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourceRepository<T, ?> getResourceRepository() {
        return resourceRepository;
    }

    @Override
    public String toString() {
        return "DirectResourceEntry{" +
            "resourceRepository=" + resourceRepository +
            '}';
    }
}
