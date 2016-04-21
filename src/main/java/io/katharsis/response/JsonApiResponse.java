package io.katharsis.response;

public class JsonApiResponse {

    private Object entity;
    private MetaInformation metaInformation;
    private LinksInformation linksInformation;

    public JsonApiResponse() {
    }

    public JsonApiResponse(JsonApiResponse jsonApiResponse) {
        this.entity = jsonApiResponse.entity;
        this.entity = jsonApiResponse.metaInformation;
        this.entity = jsonApiResponse.linksInformation;
    }

    public Object getEntity() {
        return entity;
    }

    public JsonApiResponse setEntity(Object entity) {
        this.entity = entity;
        return this;
    }

    public MetaInformation getMetaInformation() {
        return metaInformation;
    }

    public JsonApiResponse setMetaInformation(MetaInformation metaInformation) {
        this.metaInformation = metaInformation;
        return this;
    }

    public LinksInformation getLinksInformation() {
        return linksInformation;
    }

    public JsonApiResponse setLinksInformation(LinksInformation linksInformation) {
        this.linksInformation = linksInformation;
        return this;
    }
}
