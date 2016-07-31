package io.katharsis.domain.api;

import javax.annotation.Nullable;

public interface ProvidesJsonApi {

    @Nullable
    JsonApi getJsonApi();

}
