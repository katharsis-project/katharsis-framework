package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.RequestBodyDeserializer;

@JsonDeserialize(using = RequestBodyDeserializer.class)
public class RequestBody {

    /**
     * Can be either of type {@link DataBody} or {@link Iterable} of {@link DataBody}.
     */
    private Object data;

    public Object getData() {
        return data;
    }

    public DataBody getSingleData() {
        return (DataBody) data;
    }

    public Iterable<DataBody> getMultipleData() {
        //noinspection unchecked
        return (Iterable<DataBody>) data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isMultiple() {
        return data != null &&
                Iterable.class.isAssignableFrom(data.getClass());
    }
}
