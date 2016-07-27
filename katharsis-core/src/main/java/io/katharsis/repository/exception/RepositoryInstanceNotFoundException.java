package io.katharsis.repository.exception;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

/**
 * Thrown when repository instance for a resource cannot be found
 */
public final class RepositoryInstanceNotFoundException extends KatharsisMatchingException {

    public RepositoryInstanceNotFoundException(String missingRepositoryClassName) {
        super("Instance of the repository not found: " + missingRepositoryClassName);
    }
}