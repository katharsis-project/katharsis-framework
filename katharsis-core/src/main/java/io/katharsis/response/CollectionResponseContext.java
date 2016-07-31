package io.katharsis.response;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonApiPath;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@ToString
@NoArgsConstructor
public class CollectionResponseContext implements BaseResponseContext {

    private int httpStatus;
    private JsonApiResponse response;
    private JsonApiPath path;
    private QueryParams queryParams;

    public CollectionResponseContext(JsonApiResponse response, JsonApiPath path, QueryParams queryParams) {
        this.httpStatus = HttpStatus.OK_200;
        this.response = response;
        this.path = path;
        this.queryParams = queryParams;
    }

    @Override
    public JsonApiResponse getResponse() {
        return response;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public void setHttpStatus(int newStatus) {
        httpStatus = newStatus;
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
        CollectionResponseContext that = (CollectionResponseContext) o;
        return Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }
}
