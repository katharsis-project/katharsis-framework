package io.katharsis.jackson.serializer.include;

/**
 * Contains two fields by which it is possible to differentiate a resource: <i>type</i> and <i>id</i>.
 */
public class ResourceDigest {
    private Object id;
    private String type;

    public ResourceDigest(Object id, String type) {
        this.id = id;
        this.type = type;
    }

    public Object getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceDigest that = (ResourceDigest) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return type != null ? type.equals(that.type) : that.type == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
