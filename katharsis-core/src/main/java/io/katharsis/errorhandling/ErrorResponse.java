package io.katharsis.errorhandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponseBuilder;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;

public final class ErrorResponse {

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
    
    public int getHttpStatus() {
        return httpStatus;
    }

    public JsonApiResponse getResponse() {
        return new JsonApiResponse()
            .setEntity(data);
    }

    public JsonPath getJsonPath() {
        return null;
    }

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

	public io.katharsis.repository.response.Response toResponse() {
		Document responseDocument = new Document();
		List<ErrorData> errors = new ArrayList<>();
		for(ErrorData error : getErrors()){
			errors.add(error);
		}
		responseDocument.setErrors(errors);
		
		return new Response(responseDocument, getHttpStatus());
	}

}