package io.katharsis.response;

import io.katharsis.jackson.ContainerSerializer;

import java.util.Objects;

/**
 * A class responsible for representing a single data filed within top-level JSON object returned by Katharsis. The
 * resulting JSON is serialized using {@link ContainerSerializer}.
 */
public class Container<T> {
    T data;

    public Container() {
    }

    public Container(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container<?> container = (Container<?>) o;
        return Objects.equals(data, container.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
