package io.katharsis.path;

/**
 * Represent a JSON API path sent to the server. Each resource or field defined in the path is represented by one
 * ResourcePath object.
 *
 * It is represented in a form of a doubly-linked list
 */
public class ResourcePath {

    /**
     * Name of a resource or a filed
     */
    private String resourceName;

    /**
     * Unique identifier of a field
     */
    private PathIds ids;

    /**
     * If true, indicates if a path concern a relationship between current object and childResource
     */
    private boolean isRelationship;

    /**
     * Entry closer to path's beginning
     */
    private ResourcePath parentResource;

    /**
     * Entry closer to path's end
     */
    private ResourcePath childResource;

    public ResourcePath(String resourceName) {
        this(resourceName, false);
    }

    public ResourcePath(String resourceName, boolean isRelationship) {
        this(resourceName, isRelationship, null);
    }

    public ResourcePath(String resourceName, boolean isRelationship, PathIds ids) {
        this.resourceName = resourceName;
        this.isRelationship = isRelationship;
        this.ids = ids;
    }

    /**
     * Returns true if a ResourcePath concerns a collection.
     * It can happen if there's no or more than one id provided.
     */
    public boolean isCollection() {
        return ids == null || ids.getIds().size() > 1;
    }

    public String getResourceName() {
        return resourceName;
    }

    public PathIds getIds() {
        return ids;
    }

    public void setIds(PathIds ids) {
        this.ids = ids;
    }

    public ResourcePath getParentResource() {
        return parentResource;
    }

    public void setParentResource(ResourcePath parentResource) {
        this.parentResource = parentResource;
    }

    public ResourcePath getChildResource() {
        return childResource;
    }

    public void setChildResource(ResourcePath childResource) {
        this.childResource = childResource;
    }

    public boolean isRelationship() {
        return isRelationship;
    }

    public void setRelationship(boolean isRelationship) {
        this.isRelationship = isRelationship;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourcePath that = (ResourcePath) o;

        if (isRelationship != that.isRelationship) return false;
        if (ids != null ? !ids.equals(that.ids) : that.ids != null) return false;
        if (parentResource != null ? !parentResource.equals(that.parentResource) : that.parentResource != null)
            return false;
        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resourceName != null ? resourceName.hashCode() : 0;
        result = 31 * result + (ids != null ? ids.hashCode() : 0);
        result = 31 * result + (isRelationship ? 1 : 0);
        result = 31 * result + (parentResource != null ? parentResource.hashCode() : 0);
        return result;
    }
}
