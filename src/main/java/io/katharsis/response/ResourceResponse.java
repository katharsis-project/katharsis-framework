package io.katharsis.response;

import io.katharsis.queryParams.RequestParams;
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

    private RequestParams requestParams;

    private MetaInformation metaInformation;

    public ResourceResponse(MetaInformation metaInformation) {
        this.metaInformation = metaInformation;
    }

    public ResourceResponse(Object data, JsonPath jsonPath, RequestParams requestParams, MetaInformation metaInformation) {
        this.data = data;
        this.jsonPath = jsonPath;
        this.requestParams = requestParams;
        this.metaInformation = metaInformation;
    }

    @Override
    public int getHttpStatus() {
        return HttpStatus.OK_200;
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
    public RequestParams getRequestParams() {
        return requestParams;
    }

    @Override
    public MetaInformation getMetaInformation() {
        return metaInformation;
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
