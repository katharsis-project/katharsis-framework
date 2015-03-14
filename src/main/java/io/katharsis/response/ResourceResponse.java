package io.katharsis.response;

public class ResourceResponse<T> implements BaseResponse<T> {
    private T data;

    public ResourceResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
