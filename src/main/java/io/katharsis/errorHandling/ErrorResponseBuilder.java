package io.katharsis.errorHandling;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponseBuilder {
    private Iterable<ErrorObject> data;
    private int status;

    public ErrorResponseBuilder setErrorData(Iterable<ErrorObject> errorObjects) {
        this.data = data;
        return this;
    }
    public ErrorResponseBuilder setSingleErrorData(ErrorObject errorObject) {
        List<ErrorObject> errorObjects = new ArrayList<>();
        errorObjects.add(errorObject);
        this.data = errorObjects;
        return this;
    }

    public ErrorResponseBuilder setStatus(int status) {
        this.status = status;
        return this;
    }

    public ErrorResponse build() {
        return new ErrorResponse(data, status);
    }
}