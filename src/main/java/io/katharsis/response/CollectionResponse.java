package io.katharsis.response;

import java.util.List;

public class CollectionResponse implements BaseResponse<List<?>> {
    private List<?> data;

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }
}
