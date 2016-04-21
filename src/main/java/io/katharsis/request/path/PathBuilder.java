package io.katharsis.request.path;

import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Builder responsible for parsing URL path.
 */
public class PathBuilder {
    public static final String SEPARATOR = "/";
    public static final String RELATIONSHIP_MARK = "relationships";

    private final ResourceRegistry resourceRegistry;

    public PathBuilder(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    /**
     * Parses path provided by the application. The path provided cannot contain neither hostname nor protocol. It
     * can start or end with slash e.g. <i>/tasks/1/</i> or <i>tasks/1</i>.
     *
     * @param path Path to be parsed
     * @return doubly-linked list which represents path given at the input
     */
    public JsonPath buildPath(String path) {
        String[] strings = splitPath(path);
        if (strings.length == 0 || (strings.length == 1 && "".equals(strings[0]))) {
            throw new ResourceException("Path is empty");
        }

        JsonPath previousJsonPath = null, currentJsonPath = null;
        PathIds pathIds;
        boolean relationshipMark;
        String elementName;

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
            if (previousJsonPath != null) {
                currentJsonPath = getNonResourcePath(previousJsonPath, elementName, relationshipMark);
                if (pathIds != null) {
                    throw new ResourceException("RelationshipsPath and FieldPath cannot contain ids");
                }
            } else if (entry != null && !relationshipMark) {
                currentJsonPath = new ResourcePath(elementName);
            } else {
                throw new ResourceNotFoundException(path);
            }

            if (pathIds != null) {
                currentJsonPath.setIds(pathIds);
            }
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
        Set<ResourceField> resourceFields = previousEntry.getResourceInformation().getRelationshipFields();
        for (ResourceField field : resourceFields) {
            if (field.getJsonName().equals(elementName)) {
                if (relationshipMark) {
                    return new RelationshipsPath(elementName);
                } else {
                    return new FieldPath(elementName);
                }
            }
        }
        //TODO: Throw different exception? element name can be null..
        throw new ResourceFieldNotFoundException(elementName);
    }

    private static PathIds createPathIds(String idsString) {
        List<String> pathIds = Arrays.asList(idsString.split(PathIds.ID_SEPERATOR));
        return new PathIds(pathIds);
    }

    private static String[] splitPath(String path) {
        if (path.startsWith(SEPARATOR)) {
            path = path.substring(1);
        }
        if (path.endsWith(SEPARATOR)) {
            path = path.substring(0, path.length());
        }
        return path.split(SEPARATOR);
    }

    /**
     * Creates a path using the provided JsonPath structure.
     *
     * @param jsonPath JsonPath structure to be parsed
     * @return String representing structure provided in the input
     */
    public static String buildPath(JsonPath jsonPath) {
        List<String> urlParts = new LinkedList<>();

        JsonPath currentJsonPath = jsonPath;
        String pathPart;
        do {
            if (currentJsonPath instanceof RelationshipsPath) {
                pathPart = RELATIONSHIP_MARK + SEPARATOR + currentJsonPath.getElementName();
            } else if (currentJsonPath instanceof FieldPath) {
                pathPart = currentJsonPath.getElementName();
            } else {
                pathPart = currentJsonPath.getElementName();
                if (currentJsonPath.getIds() != null) {
                    pathPart += SEPARATOR + mergeIds(currentJsonPath.getIds());
                }
            }
            urlParts.add(pathPart);

            currentJsonPath = currentJsonPath.getParentResource();
        } while (currentJsonPath != null);
        Collections.reverse(urlParts);

        return SEPARATOR + StringUtils.join(SEPARATOR, urlParts) + SEPARATOR;
    }

    private static String mergeIds(PathIds ids) {
        return StringUtils.join(PathIds.ID_SEPERATOR, ids.getIds());
    }
}
