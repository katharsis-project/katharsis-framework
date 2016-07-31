package io.katharsis.domain;

import io.katharsis.domain.api.DataResponse;
import io.katharsis.domain.api.JsonApi;
import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import io.katharsis.domain.api.Resource;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Primary data MUST be either:
 * - a single resource object, a single resource identifier object, or null, for requests that target single resources
 * - an array of resource objects, an array of resource identifier objects, or an empty array ([]), for requests that target resource collections
 * <p/>
 * http://jsonapi.org/format/#document-top-level
 */
@Data
public class CollectionResponse implements DataResponse {

    protected Iterable<Object> data;
    private MetaInformation meta;
    private JsonApi jsonApi;
    private LinksInformation links;
    private Collection<Resource> included;

    public CollectionResponse(Iterable<Object> data,
                              @Nullable MetaInformation meta,
                              @Nullable JsonApi jsonApi,
                              @Nullable LinksInformation links,
                              @Nullable List<Resource> included) {
        this.data = data;
        this.meta = meta;
        this.jsonApi = jsonApi;
        this.links = links;
        this.included = included;
    }

}
