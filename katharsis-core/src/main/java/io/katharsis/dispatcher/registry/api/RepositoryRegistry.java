package io.katharsis.dispatcher.registry.api;

import io.katharsis.dispatcher.registry.annotated.AnnotatedResourceRepositoryAdapter;
import io.katharsis.repository.exception.RepositoryNotFoundException;

/**
 * Returns an instance of a Repository for a given resource name.
 */
public interface RepositoryRegistry {

    AnnotatedResourceRepositoryAdapter get(String resource) throws RepositoryNotFoundException;

}
