package io.katharsis.repository.exception;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

/**
 * Thrown when repository definition for a resource cannot be found in specified package.
 */
public final class RepositoryNotFoundException extends KatharsisMatchingException {
    public RepositoryNotFoundException(Class clazz) {
        super("Repository for a resource not found: " + clazz.getCanonicalName());
    }
}
