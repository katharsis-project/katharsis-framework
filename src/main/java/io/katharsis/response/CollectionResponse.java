package io.katharsis.response;

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
}
