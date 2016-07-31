package io.katharsis.utils.java;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.NoSuchElementException;
import java.util.Objects;

@ToString
@EqualsAndHashCode
public class Optional<T> {
    private static final Optional<?> EMPTY = new Optional<>(null);

    private final T value;

    private Optional(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    public static <T> Optional<T> of(T value) {
        Objects.requireNonNull(value);
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? Optional.<T>empty() : of(value);
    }

    public boolean isPresent() {
        return value != null;
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

}
