package io.katharsis.legacy.queryParams.include;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a single inclusion passed as a query param. An example of the value represented by this value is:
 * <i>comments.author</i>.
 */
public class Inclusion implements Comparable<Inclusion>{

    private String path;
    private List<String> pathList;

    public Inclusion(@SuppressWarnings("SameParameterValue") String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        this.path = path;
        this.pathList = Arrays.asList(path.split("\\."));
    }

    public String getPath() {
        return path;
    }

    public List<String> getPathList() {
        return pathList;
    }

    @Override
    public int compareTo(Inclusion o) {
        if(o == null) return 1;
        if(this.path == null)
            return o.path == null ? 0 : -1;
        if(o.path == null) return 1;
        return this.path.compareTo(o.getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Inclusion inclusion = (Inclusion) o;

        return !(path != null ? !path.equals(inclusion.path) : inclusion.path != null);

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Inclusion{" +
                "path='" + path + '\'' +
                '}';
    }
}
