package io.katharsis.response;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonPath;

import java.util.Objects;

/**
 * A class responsible for representing top-level JSON object returned by Katharsis. The data value is a single object.
 * The resulting JSON is shown below:
 * <pre>
 * {@code
 * {
 *   data: null,
 * }
 * }
 * </pre>
 */
public class ResourceResponse implements BaseResponse {

    /**
     * The type of the field should be either {@link Container} or a list of {@link Container}
     */
    private Object data;

    private JsonPath jsonPath;

    private QueryParams queryParams;

    private MetaInformation metaInformation;

    private LinksInformation linksInformation;

    private int httpStatus;

    public ResourceResponse(MetaInformation metaInformation, LinksInformation linksInformation, int httpStatus) {
        this(null, null, null, metaInformation, linksInformation, httpStatus);
    }

    public ResourceResponse(Object data, JsonPath jsonPath, QueryParams queryParams,
                            MetaInformation metaInformation, LinksInformation linksInformation) {
        this(data, jsonPath, queryParams, metaInformation, linksInformation, HttpStatus.OK_200);
    }

    public ResourceResponse(Object data, JsonPath jsonPath, QueryParams queryParams,
                            MetaInformation metaInformation, LinksInformation linksInformation, int httpStatus) {
        this.data = data;
        this.jsonPath = jsonPath;
        this.queryParams = queryParams;
        this.metaInformation = metaInformation;
        this.linksInformation = linksInformation;
        this.httpStatus = httpStatus;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public JsonPath getJsonPath() {
        return jsonPath;
    }

    @Override
    public QueryParams getQueryParams() {
        return queryParams;
    }

    @Override
    public MetaInformation getMetaInformation() {
        return metaInformation;
    }

    @Override
    public LinksInformation getLinksInformation() {
        return linksInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceResponse that = (ResourceResponse) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
