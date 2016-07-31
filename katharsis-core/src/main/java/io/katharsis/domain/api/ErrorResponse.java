package io.katharsis.domain.api;

import java.util.Collection;

public interface ErrorResponse extends TopLevel {

    Collection<ErrorObject> getErrors();

}
