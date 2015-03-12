package io.katharsis.path;

public class ResourcePath<ID> {

    private String resourceName;
    private PathIds<ID> ids;
    private ResourcePath<?> parentResource;
    private ResourcePath<?> childResource;

    public ResourcePath() {
    }

    public ResourcePath(String resourceName, PathIds<ID> ids) {
        this.resourceName = resourceName;
        this.ids = ids;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public PathIds<ID> getIds() {
        return ids;
    }

    public void setIds(PathIds<ID> ids) {
        this.ids = ids;
    }

    public ResourcePath<?> getParentResource() {
        return parentResource;
    }

    public void setParentResource(ResourcePath<?> parentResource) {
        this.parentResource = parentResource;
    }

    public ResourcePath<?> getChildResource() {
        return childResource;
    }

    public void setChildResource(ResourcePath<?> childResource) {
        this.childResource = childResource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourcePath that = (ResourcePath) o;

        if (ids != null ? !ids.equals(that.ids) : that.ids != null) return false;
        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resourceName != null ? resourceName.hashCode() : 0;
        result = 31 * result + (ids != null ? ids.hashCode() : 0);
        return result;
    }
}
