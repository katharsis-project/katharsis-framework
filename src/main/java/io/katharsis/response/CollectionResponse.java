package io.katharsis.response;

import java.util.Objects;

public class CollectionResponse implements BaseResponse<Iterable> {

    private Iterable data;

    public CollectionResponse() {
    }

    public CollectionResponse(Iterable data) {
        this.data = data;
    }

    public Iterable getData() {
        return data;
    }

    public void setData(Iterable data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionResponse that = (CollectionResponse) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
