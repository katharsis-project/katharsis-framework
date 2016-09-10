package io.katharsis.response;

import java.util.Objects;

import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.request.path.JsonPath;

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

    private QueryAdapter queryAdapter;

    public CollectionResponseContext() {
    }

    public CollectionResponseContext(JsonApiResponse response, JsonPath jsonPath, QueryAdapter queryAdapter) {
        this.response = response;
        this.jsonPath = jsonPath;
        this.queryAdapter = queryAdapter;
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
    public QueryAdapter getQueryAdapter() {
        return queryAdapter;
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
