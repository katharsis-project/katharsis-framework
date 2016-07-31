package io.katharsis.domain;

import io.katharsis.domain.api.JsonApi;
import io.katharsis.domain.api.MetaInformation;
import lombok.Value;

@Value
public class JsonApiImpl implements JsonApi {

    public static final String SPEC_VERSION = "1.0";

    private MetaInformation meta;

    @Override
    public String getVersion() {
        return SPEC_VERSION;
    }

    @Override
    public MetaInformation getMeta() {
        return meta;
    }
}
