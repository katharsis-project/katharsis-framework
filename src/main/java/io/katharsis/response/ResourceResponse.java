package io.katharsis.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class ResourceResponse implements BaseResponse {

    /**
     * The type of the field should be either {@link Container} or a list of {@link Container}
     */
    private Object data;

    public ResourceResponse() {
    }

    public ResourceResponse(Object data) {
        this.data = data;
    }

    @Override
    @JsonIgnore //TODO: Is it necessary?
    public int getStatus() {
        //TODO: gather status constants
        return 200;
    }

    @Override
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
        ResourceResponse that = (ResourceResponse) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
