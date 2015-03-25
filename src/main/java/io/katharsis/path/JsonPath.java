package io.katharsis.path;

/**
 * Represent a JSON API path sent to the server. Each resource or field defined in the path is represented by one
 * ResourcePath object.
 *
 * It is represented in a form of a doubly-linked list
 */
public class JsonPath {

    /**
     * Name of a resource or a filed
     */
    private String elementName;

    /**
     * Unique identifier of a field
     */
    private PathIds ids;

    /**
     * If true, indicates if a path concern a relationship between current object and childResource
     */
    private boolean hasRelationshipMark;

    /**
     * Entry closer to path's beginning
     */
    private JsonPath parentResource;

    /**
     * Entry closer to path's end
     */
    private JsonPath childResource;

    public JsonPath(String elementName) {
        this(elementName, false);
    }

    public JsonPath(String elementName, boolean hasRelationshipMark) {
        this(elementName, hasRelationshipMark, null);
    }

    public JsonPath(String elementName, boolean hasRelationshipMark, PathIds ids) {
        this.elementName = elementName;
        this.hasRelationshipMark = hasRelationshipMark;
        this.ids = ids;
    }

    /**
     * Returns true if a JsonPath concerns a collection.
     * It can happen if there's no or more than one id provided.
     */
    public boolean isCollection() {
        JsonPath testPath = this;
        if (isRelationship()) {
            testPath = parentResource;
        }
        return testPath.ids == null || testPath.ids.getIds().size() > 1;
    }

    /**
     * Returns name of the current element. It can be either resource type or resource's field.
     *
     * @return name of the element
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Returns name of a resource the last resource in requested path.
     * There can be paths that concern relations. In this case a elementName from parent JsonPath should be retrieved.
     *
     * @return nam of the lase resource
     */
    public String getResourceName() {
        String resourceName = this.elementName;
        if (isRelationship()) {
            resourceName = parentResource.elementName;
        }
        return resourceName;
    }

    public PathIds getIds() {
        return ids;
    }

    public void setIds(PathIds ids) {
        this.ids = ids;
    }

    public JsonPath getParentResource() {
        return parentResource;
    }

    public void setParentResource(JsonPath parentResource) {
        this.parentResource = parentResource;
    }

    public JsonPath getChildResource() {
        return childResource;
    }

    public void setChildResource(JsonPath childResource) {
        this.childResource = childResource;
    }

    public boolean isHasRelationshipMark() {
        return hasRelationshipMark;
    }

    public void setHasRelationshipMark(boolean isRelationship) {
        this.hasRelationshipMark = isRelationship;
    }

    public boolean isRelationship() {
        return parentResource != null && parentResource.hasRelationshipMark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonPath that = (JsonPath) o;

        if (hasRelationshipMark != that.hasRelationshipMark) return false;
        if (ids != null ? !ids.equals(that.ids) : that.ids != null) return false;
        if (parentResource != null ? !parentResource.equals(that.parentResource) : that.parentResource != null)
            return false;
        if (elementName != null ? !elementName.equals(that.elementName) : that.elementName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = elementName != null ? elementName.hashCode() : 0;
        result = 31 * result + (ids != null ? ids.hashCode() : 0);
        result = 31 * result + (hasRelationshipMark ? 1 : 0);
        result = 31 * result + (parentResource != null ? parentResource.hashCode() : 0);
        return result;
    }
}
