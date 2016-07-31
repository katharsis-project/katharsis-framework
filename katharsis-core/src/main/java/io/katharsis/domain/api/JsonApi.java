package io.katharsis.domain.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface JsonApi extends ProvidesMeta {

    String getVersion();

}
