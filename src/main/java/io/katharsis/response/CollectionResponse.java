package io.katharsis.response;

public class CollectionResponse implements BaseResponse<Iterable<?>> {
    private Iterable<?> data;

    public CollectionResponse(Iterable<?> data) {
        this.data = data;
    }

    public Iterable<?> getData() {
        return data;
    }

    public void setData(Iterable<?> data) {
        this.data = data;
    }
}
