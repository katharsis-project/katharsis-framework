package io.katharsis.response;

import io.katharsis.queryParams.QueryParams;
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
public class CollectionResponseContext implements BaseResponseContext {

    private JsonApiResponse response;

    private JsonPath jsonPath;

    private QueryParams queryParams;

    public CollectionResponseContext() {
    }

    public CollectionResponseContext(JsonApiResponse response, JsonPath jsonPath, QueryParams queryParams) {
        this.response = response;
        this.jsonPath = jsonPath;
        this.queryParams = queryParams;
    }

    @Override
    public JsonApiResponse getResponse() {
        return response;
    }

    @Override
    public int getHttpStatus() {
        return HttpStatus.OK_200;
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
        CollectionResponseContext that = (CollectionResponseContext) o;
        return Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }
}
