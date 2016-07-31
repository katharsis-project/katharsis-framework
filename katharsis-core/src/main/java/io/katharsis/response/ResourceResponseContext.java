package io.katharsis.response;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonApiPath;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@ToString
public class ResourceResponseContext implements BaseResponseContext {

    /**
     * The type of the field should be either {@link Container} or a list of {@link Container}
     */
    private JsonApiResponse response;

    private JsonApiPath path;

    private QueryParams queryParams;

    @Getter
    @Setter
    private int httpStatus;

    public ResourceResponseContext(JsonApiResponse response, int httpStatus) {
        this(response, null, null, httpStatus);
    }

    public ResourceResponseContext(JsonApiResponse response, JsonApiPath jsonPath, QueryParams queryParams) {
        this(response, jsonPath, queryParams, HttpStatus.OK_200);
    }

    public ResourceResponseContext(JsonApiResponse response, JsonApiPath path, QueryParams queryParams, int httpStatus) {
        this.response = response;
        this.path = path;
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
    public JsonApiPath getPath() {
        return path;
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
