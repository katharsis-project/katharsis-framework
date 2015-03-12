package io.katharsis.path;

import java.util.HashSet;
import java.util.Set;

public class PathIds<ID> {
    private Set<ID> ids = new HashSet<>(1);

    public PathIds() {
    }

    public PathIds(ID id) {
        ids.add(id);
    }

    public Set<ID> getIds() {
        return ids;
    }

    public void setIds(Set<ID> ids) {
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
