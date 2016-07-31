package io.katharsis.errorhandling;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import lombok.ToString;

import java.util.Objects;

@ToString
public final class ErrorResponse implements BaseResponseContext {

    public static final String ERRORS = "errors";

    private final Iterable<ErrorData> data;
    private final int httpStatus;

    public ErrorResponse(Iterable<ErrorData> data, int httpStatus) {
        this.data = data;
        this.httpStatus = httpStatus;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public void setHttpStatus(int newStatus) {
        //TODO: ieugen: refactor this empyt method
    }

    @Override
    public JsonApiResponse getResponse() {
        return new JsonApiResponse()
                .setEntity(data);
    }

    @Override
    public JsonApiPath getPath() {
        return null;
//        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public QueryParams getQueryParams() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorResponse)) {
            return false;
        }
        ErrorResponse that = (ErrorResponse) o;
        return Objects.equals(httpStatus, that.httpStatus) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, httpStatus);
    }
}