package io.katharsis.domain;

import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import io.katharsis.domain.api.Relationship;
import io.katharsis.domain.api.ResourceId;
import lombok.Data;

import javax.annotation.Nullable;

@Data
public class SimpleToOneRelationship implements Relationship {

    ResourceId id;

    @Nullable
    @Override
    public ResourceId getData() {
        return id;
    }

    @Override
    public LinksInformation getLinks() {
        return null;
    }

    @Override
    public MetaInformation getMeta() {
        return null;
    }
}
