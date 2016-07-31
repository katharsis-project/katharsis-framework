package io.katharsis.domain.api;

import javax.annotation.Nullable;

public interface ProvidesLinks {

    @Nullable
    LinksInformation getLinks();

}
