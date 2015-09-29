package io.katharsis.response;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.path.JsonPath;

import java.util.Objects;

/**
 * A class responsible for representing top-level JSON object returned by Katharsis. The data value is an array. The
 * resulting JSON is shown below:
 * <pre>
 * {@code
 * {
 *   data: [],
 * }
 * }
 * </pre>
 */
public class CollectionResponse implements BaseResponse<Iterable> {

    private Iterable data;

    private JsonPath jsonPath;

    private RequestParams requestParams;

    private MetaInformation metaInformation;

    private LinksInformation linksInformation;

    public CollectionResponse() {
    }

    public CollectionResponse(Iterable data, JsonPath jsonPath, RequestParams requestParams,
        MetaInformation metaInformation, LinksInformation linksInformation) {
        this.data = data;
        this.jsonPath = jsonPath;
        this.requestParams = requestParams;
        this.metaInformation = metaInformation;
        this.linksInformation = linksInformation;
    }

    @Override
    public int getHttpStatus() {
        return HttpStatus.OK_200;
    }

    @Override
    public Iterable getData() {
        return data;
    }

    @Override
    public JsonPath getJsonPath() {
        return jsonPath;
    }

    @Override
    public RequestParams getRequestParams() {
        return requestParams;
    }

    @Override
    public MetaInformation getMetaInformation() {
        return metaInformation;
    }

    @Override
    public LinksInformation getLinksInformation() {
        return linksInformation;
    }

    public void setData(Iterable data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionResponse that = (CollectionResponse) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
