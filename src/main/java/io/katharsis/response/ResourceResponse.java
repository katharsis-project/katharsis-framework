package io.katharsis.response;

import java.util.Objects;

public class ResourceResponse<T> implements BaseResponse<Container<T>> {
    private Container<T> data;

    public ResourceResponse() {
    }

    public ResourceResponse(Container<T> data) {
        this.data = data;
    }

    public Container<T> getData() {
        return data;
    }

    public void setData(Container<T> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceResponse<?> that = (ResourceResponse<?>) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
