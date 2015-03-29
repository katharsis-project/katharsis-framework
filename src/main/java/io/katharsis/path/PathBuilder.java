package io.katharsis.path;

import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Builder responsible for parsing URL path.
 */
public class PathBuilder {
    public static final String SEPARATOR = "/";
    public static final String RELATIONSHIP_MARK = "links";

    private ResourceRegistry resourceRegistry;

    public PathBuilder(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    public JsonPath buildPath(String path) {
        String[] strings = splitPath(path);
        if (strings.length == 0 || (strings.length == 1 && "".equals(strings[0]))) {
            throw new IllegalArgumentException("Path is empty");
        }

        JsonPath previousJsonPath = null, currentJsonPath = null;
        PathIds pathIds;
        boolean relationshipMark;
        String elementName = null;

        for (int currentElementIdx = 0; currentElementIdx < strings.length; ) {
            elementName = null;
            pathIds = null;
            relationshipMark = false;

            if (RELATIONSHIP_MARK.equals(strings[currentElementIdx])) {
                relationshipMark = true;
                currentElementIdx++;
            }

            if (currentElementIdx < strings.length && !RELATIONSHIP_MARK.equals(strings[currentElementIdx])) {
                elementName = strings[currentElementIdx];
                currentElementIdx++;
            }

            if (currentElementIdx < strings.length && !RELATIONSHIP_MARK.equals(strings[currentElementIdx])) {
                pathIds = createPathIds(strings[currentElementIdx]);
                currentElementIdx++;
            }
            RegistryEntry entry = resourceRegistry.getEntry(elementName);
            if (entry != null) {
                currentJsonPath = new ResourcePath(elementName);
            } else if (previousJsonPath != null) {
                currentJsonPath = getNonResourcePath(previousJsonPath, elementName, relationshipMark);
            } else {
                throw new IllegalArgumentException("Invalid path: " + path);
            }

            currentJsonPath.setIds(pathIds);
            if (previousJsonPath != null) {
                previousJsonPath.setChildResource(currentJsonPath);
                currentJsonPath.setParentResource(previousJsonPath);
            }
            previousJsonPath = currentJsonPath;
        }

        return currentJsonPath;
    }

    private JsonPath getNonResourcePath(JsonPath previousJsonPath, String elementName, boolean relationshipMark) {
        String previousElementName = previousJsonPath.getElementName();
        RegistryEntry previousEntry = resourceRegistry.getEntry(previousElementName);
        Set<Field> resourceFields = previousEntry.getResourceInformation().getAllFields();
        for (Field field : resourceFields) {
            if (field.getName().equals(elementName)) {
                if (relationshipMark) {
                    return new LinksPath(elementName);
                } else {
                    return new FieldPath(elementName);
                }
            }
        }
        throw new IllegalArgumentException("No type field defined after links marker");
    }

    private PathIds createPathIds(String idsString) {
        List<String> pathIds = Arrays.asList(idsString.split(PathIds.ID_SEPERATOR));
        return new PathIds(pathIds);
    }

    private String[] splitPath(String path) {
        if (path.startsWith(SEPARATOR)) {
            path = path.substring(1);
        }
        if (path.endsWith(SEPARATOR)) {
            path = path.substring(0, path.length());
        }
        return path.split(SEPARATOR);
    }
}
