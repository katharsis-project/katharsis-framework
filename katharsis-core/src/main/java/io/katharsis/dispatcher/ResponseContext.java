package io.katharsis.dispatcher;

import io.katharsis.domain.api.TopLevel;

public interface ResponseContext {

    int getHttpStatus();

    void setHttpStatus(int status);

    TopLevel getDocument();

    void setDocument(TopLevel document);

}
