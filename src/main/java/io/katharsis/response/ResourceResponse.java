package io.katharsis.response;

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
}
