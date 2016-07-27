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
public class ResourceResponseContext implements BaseResponseContext {

    /**
     * The type of the field should be either {@link Container} or a list of {@link Container}
     */
    private JsonApiResponse response;

    private JsonPath jsonPath;

    private QueryParams queryParams;

    private int httpStatus;

    public ResourceResponseContext(JsonApiResponse response, int httpStatus) {
        this(response, null, null, httpStatus);
    }

    public ResourceResponseContext(JsonApiResponse response, JsonPath jsonPath, QueryParams queryParams) {
        this(response, jsonPath, queryParams, HttpStatus.OK_200);
    }

    public ResourceResponseContext(JsonApiResponse response, JsonPath jsonPath, QueryParams queryParams, int httpStatus) {
        this.response = response;
        this.jsonPath = jsonPath;
        this.queryParams = queryParams;
        this.httpStatus = httpStatus;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public JsonApiResponse getResponse() {
        return response;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceResponseContext that = (ResourceResponseContext) o;
        return Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }
}
