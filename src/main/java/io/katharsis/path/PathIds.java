package io.katharsis.path;

import java.util.HashSet;
import java.util.Set;

/**
 * Represent an id or ids passed in the path from a client.
 */
public class PathIds {
    private Set<String> ids = new HashSet<>(1);

    public PathIds() {
    }

    public PathIds(String id) {
        ids.add(id);
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathIds pathIds = (PathIds) o;

        if (ids != null ? !ids.equals(pathIds.ids) : pathIds.ids != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ids != null ? ids.hashCode() : 0;
    }
}
