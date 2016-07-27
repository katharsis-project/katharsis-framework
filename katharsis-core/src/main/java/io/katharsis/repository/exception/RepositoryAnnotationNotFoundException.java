package io.katharsis.repository.exception;

import io.katharsis.errorhandling.exception.KatharsisMatchingException;

public class RepositoryAnnotationNotFoundException extends KatharsisMatchingException {

    public RepositoryAnnotationNotFoundException(String message) {
        super(message);
    }
}
