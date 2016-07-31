package io.katharsis.repository.exception;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import lombok.NonNull;

/**
 * Thrown when repository definition for a resource cannot be found in specified package.
 */
public final class RepositoryNotFoundException extends KatharsisMatchingException {

    public RepositoryNotFoundException(@NonNull Class clazz) {
        super("Repository for a resource not found: " + clazz.getCanonicalName());
    }

    public RepositoryNotFoundException(@NonNull String resourceName) {
        super("Repository for a resource not found: " + resourceName);
    }
}
