package io.katharsis.response;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
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

    private QueryAdapter queryAdapter;

    private int httpStatus;

    public ResourceResponseContext(JsonApiResponse response, int httpStatus) {
        this(response, null, (QueryParamsAdapter)null, httpStatus);
    }

    public ResourceResponseContext(JsonApiResponse response, JsonPath jsonPath, QueryParams queryParams) {
    	this(response, jsonPath, new QueryParamsAdapter(queryParams));
    }
    
    public ResourceResponseContext(JsonApiResponse response, JsonPath jsonPath, QueryAdapter queryAdapter) {
        this(response, jsonPath, queryAdapter, HttpStatus.OK_200);
    }
    
    public ResourceResponseContext(JsonApiResponse response, JsonPath jsonPath, QueryAdapter queryAdapter, int httpStatus) {
        this.response = response;
        this.jsonPath = jsonPath;
        this.queryAdapter = queryAdapter;
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
        ResourceResponseContext that = (ResourceResponseContext) o;
        return Objects.equals(response, that.response) && that.httpStatus == httpStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }
}
