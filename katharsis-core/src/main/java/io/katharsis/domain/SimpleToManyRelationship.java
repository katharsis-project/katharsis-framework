package io.katharsis.domain;

import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import io.katharsis.domain.api.Relationship;
import io.katharsis.domain.api.ResourceId;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.List;

@Data
public class SimpleToManyRelationship implements Relationship {

    private List<ResourceId> ids;

    @Nullable
    @Override
    public List<ResourceId> getData() {
        return ids;
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
