package io.katharsis.response;

import java.util.Objects;

public class CollectionResponse<T> implements BaseResponse<Iterable<Container<T>>> {

    private Iterable<Container<T>> data;

    public CollectionResponse() {
    }

    public CollectionResponse(Iterable<Container<T>> data) {
        this.data = data;
    }

    public Iterable<Container<T>> getData() {
        return data;
    }

    public void setData(Iterable<Container<T>> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionResponse<?> that = (CollectionResponse<?>) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
