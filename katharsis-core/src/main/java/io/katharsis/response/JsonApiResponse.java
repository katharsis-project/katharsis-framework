package io.katharsis.response;

import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JsonApiResponse {

    private Object entity;
    private MetaInformation metaInformation;
    private LinksInformation linksInformation;

    public JsonApiResponse(JsonApiResponse jsonApiResponse) {
        this.entity = jsonApiResponse.entity;
        this.entity = jsonApiResponse.metaInformation;
        this.entity = jsonApiResponse.linksInformation;
    }

    public JsonApiResponse setEntity(Object entity) {
        this.entity = entity;
        return this;
    }

    public JsonApiResponse setMetaInformation(MetaInformation metaInformation) {
        this.metaInformation = metaInformation;
        return this;
    }

    public JsonApiResponse setLinksInformation(LinksInformation linksInformation) {
        this.linksInformation = linksInformation;
        return this;
    }
}
