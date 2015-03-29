package io.katharsis.path;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/links/filed the first element
 * will be an object of ResourcePath class and the second will be of RelationPath type.
 */
public class LinksPath extends FieldPath {

    public LinksPath(String elementName) {
        super(elementName);
    }
}
