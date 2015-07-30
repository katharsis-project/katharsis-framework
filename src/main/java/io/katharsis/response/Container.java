package io.katharsis.response;

import io.katharsis.jackson.serializer.ContainerSerializer;
import io.katharsis.queryParams.RequestParams;

import java.util.Objects;

/**
 * A class responsible for representing a single data filed within top-level JSON object returned by Katharsis. The
 * resulting JSON is serialized using {@link ContainerSerializer}.
 */
public class Container {
    private Object data;
    private RequestParams requestParams;

    public Container() {
    }

    public Container(Object data, RequestParams requestParams) {
        this.data = data;
        this.requestParams = requestParams;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(RequestParams requestParams) {
        this.requestParams = requestParams;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(data, container.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
