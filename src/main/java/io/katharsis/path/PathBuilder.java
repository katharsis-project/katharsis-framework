package io.katharsis.path;

/**
 * Builder responsible for parsing URL path.
 */
public class PathBuilder {
    public static final String SEPARATOR = "/";
    public static final String RELATIONSHIP_MARK = "links";

    public ResourcePath buildPath(String path) {
        String[] strings = splitPath(path);

        ResourcePath previousResourcePath = null, currentResourcePath = null;
        
        int currentElementIdx = 0;
        while(true) {
            if (currentElementIdx >= strings.length) {
                break;
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
                currentResourcePath.setIds(new PathIds(strings[currentElementIdx]));
                currentElementIdx++;
            }
            
            if (currentElementIdx >= strings.length) {
                break;
            } else if (RELATIONSHIP_MARK.equals(strings[currentElementIdx])){
                currentResourcePath.setRelationship(true);
                currentElementIdx++;
            }
        }

        return currentResourcePath;
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
