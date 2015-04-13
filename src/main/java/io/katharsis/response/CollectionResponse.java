package io.katharsis.response;

import java.util.Objects;

/**
 * A class responsible for representing top-level JSON object returned by Katharsis. The data value is an array. The
 * resulting JSON is shown below:
 * <pre>
 * {@code
 * {
 *   data: [],
 * }
 * }
 * </pre>
 */
public class CollectionResponse implements BaseResponse<Iterable> {

    private Iterable data;
    private Object links;

    public CollectionResponse(Iterable data, Object links) {
        this.data = data;
        this.links = links;
    }

    public Iterable getData() {
        return data;
    }

    @Override
    public Object getLinks() {
        return links;
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
