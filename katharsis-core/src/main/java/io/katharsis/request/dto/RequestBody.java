package io.katharsis.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.RequestBodyDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@NoArgsConstructor
@JsonDeserialize(using = RequestBodyDeserializer.class)
public class RequestBody {
    /**
     * Can be either of type {@link DataBody} or {@link Iterable} of {@link DataBody}.
     */
    @Getter
    @Setter
    private Object data;

    public RequestBody(@NonNull Object data) {
        this.data = data;
    }

    @JsonIgnore
    public DataBody getSingleData() {
        return (DataBody) data;
    }

    @JsonIgnore
    public Iterable<DataBody> getMultipleData() {
        //noinspection unchecked
        return (Iterable<DataBody>) data;
    }

    @JsonIgnore
    public boolean isMultiple() {
        return data != null && Iterable.class.isAssignableFrom(data.getClass());
    }
}
