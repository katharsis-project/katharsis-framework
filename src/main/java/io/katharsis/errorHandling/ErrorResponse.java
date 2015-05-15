package io.katharsis.errorHandling;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.katharsis.response.BaseResponse;


public class ErrorResponse implements BaseResponse<Iterable<ErrorObject>> {

    @JsonProperty("errors")
    private final Iterable<ErrorObject> data;
    @JsonIgnore
    private final int status;

    public ErrorResponse(Iterable<ErrorObject> data, int status) {
        this.data = data;
        this.status = status;
    }

    public static ErrorResponseBuilder newBuilder() {
        return new ErrorResponseBuilder();
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public final Iterable<ErrorObject> getData() {
        return data;
    }


}
