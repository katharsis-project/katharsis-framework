package io.katharsis.response;

public class CollectionResponse<T> implements BaseResponse<Iterable<T>> {
    private Iterable<T> data;

    public CollectionResponse() {
    }

    public CollectionResponse(Iterable<T> data) {
        this.data = data;
    }

    public Iterable<T> getData() {
        return data;
    }

    public void setData(Iterable<T> data) {
        this.data = data;
    }
}
