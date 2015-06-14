package io.katharsis.repository;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;

/**
 * Thrown when repository definition for a resource cannot be found in specified package.
 */
public final class RepositoryNotFoundException extends KatharsisInitializationException {

    public RepositoryNotFoundException(String missingRepositoryClassName) {
        super("Instance of the repository not found: " + missingRepositoryClassName);
    }
}