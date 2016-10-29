package io.katharsis.errorhandling;

import java.util.Collections;
import java.util.Objects;

import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;

public final class ErrorResponse implements BaseResponseContext {

    public static final String ERRORS = "errors";

    private final Iterable<ErrorData> data;
    private final int httpStatus;

    public ErrorResponse(Iterable<ErrorData> data, int httpStatus) {
        this.data = data;
        this.httpStatus = httpStatus;
    }

    public Iterable<ErrorData> getErrors(){
    	if(data == null){
    		return Collections.emptyList();
    	}
    	return data;
    }
    
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public JsonApiResponse getResponse() {
        return new JsonApiResponse()
            .setEntity(data);
    }

    @Override
    public JsonPath getJsonPath() {
        return null;
    }

    @Override
    public QueryAdapter getQueryAdapter() {
        return null;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
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