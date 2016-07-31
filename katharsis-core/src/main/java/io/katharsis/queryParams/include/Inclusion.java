package io.katharsis.queryParams.include;

import lombok.Value;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a single inclusion passed as a query param. An example of the value represented by this value is:
 * <i>comments.author</i>.
 */
@Value
public class Inclusion {

    private String path;

    public Inclusion(@SuppressWarnings("SameParameterValue") String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        this.path = path;
    }

    public boolean isNestedPath() {
        return path.contains(".");
    }

    public List<String> getPathList() {
        return Arrays.asList(path.split("\\."));
    }

}
