package io.katharsis.path;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/filed the first element will be
 * an object of ResourcePath type and the second will be of FieldPath type.
 */
public class FieldPath extends JsonPath {

    public FieldPath(String elementName) {
        super(elementName);
    }

    public FieldPath(String elementName, PathIds pathIds) {
        super(elementName, pathIds);
    }

    @Override
    public boolean isCollection() {
        return parentResource.ids == null || parentResource.ids.getIds().size() > 1;
    }

    @Override
    public String getResourceName() {
        return parentResource.elementName;
    }
}
