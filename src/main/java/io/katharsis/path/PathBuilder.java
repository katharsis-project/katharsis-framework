package io.katharsis.path;

import java.util.Arrays;
import java.util.List;

/**
 * Builder responsible for parsing URL path.
 */
public class PathBuilder {
    public static final String SEPARATOR = "/";
    public static final String RELATIONSHIP_MARK = "links";

    public ResourcePath buildPath(String path) {
        String[] strings = splitPath(path);
        if (strings.length == 0 || (strings.length == 1 && "".equals(strings[0]))) {
            throw new IllegalArgumentException("Path is empty");
        }

        ResourcePath previousResourcePath = null, currentResourcePath = null;

        int currentElementIdx = 0;
        while (true) {
            if (currentElementIdx >= strings.length) {
                throw new IllegalArgumentException("No type field defined after links marker");
            }

            currentResourcePath = new ResourcePath(strings[currentElementIdx]);
            currentElementIdx++;

            if (previousResourcePath != null) {
                previousResourcePath.setChildResource(currentResourcePath);
                currentResourcePath.setParentResource(previousResourcePath);
            }
            previousResourcePath = currentResourcePath;

            if (currentElementIdx >= strings.length) {
                break;
            } else {
                PathIds pathIds = createPathIds(strings[currentElementIdx]);
                currentResourcePath.setIds(pathIds);
                currentElementIdx++;
            }

            if (currentElementIdx >= strings.length) {
                break;
            } else if (RELATIONSHIP_MARK.equals(strings[currentElementIdx])) {
                currentResourcePath.setRelationship(true);
                currentElementIdx++;
            }
        }

        return currentResourcePath;
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
