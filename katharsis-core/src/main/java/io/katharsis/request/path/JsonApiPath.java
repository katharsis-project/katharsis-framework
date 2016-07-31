package io.katharsis.request.path;


import io.katharsis.errorhandling.GenericKatharsisException;
import io.katharsis.utils.java.Optional;
import lombok.Value;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A JSON API Path has between 1-4 elements
 * <p/>
 * / resource / id(s) / field | "relationships" / relationship
 * <p/>
 * This class parses a Path and identifies all parts.
 */
@Value
public class JsonApiPath {

    public static final String DEFAULT_ID_SEPARATOR = ",";
    public static final String SEPARATOR = "/";
    public static final String RELATIONSHIP_MARK = "relationships";

    private String resource;
    private Optional<List<String>> ids;
    private Optional<String> relationship;
    private Optional<String> field;
    private Optional<String> query;

    private JsonApiPath(String resource, List<String> ids, String relationship, String field, String query) {
        this(resource, Optional.ofNullable(ids), Optional.ofNullable(relationship),
                Optional.ofNullable(field), Optional.ofNullable(query));
    }

    private JsonApiPath(String resource,
                        Optional<List<String>> ids,
                        Optional<String> relationship,
                        Optional<String> field,
                        Optional<String> query) {
        this.resource = resource;
        this.ids = ids;
        this.relationship = relationship;
        this.field = field;
        this.query = query;
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
     * Parses JSON path from URL.
     *
     * @param path
     * @return
     */
    public static JsonApiPath parsePath(URL path) {
        return parsePathFromStringUrl(path, "/");
    }

    public static JsonApiPath parsePathFromStringUrl(String url) {
        return parsePathFromStringUrl(url, "/");
    }

    /**
     * Parses the path by converting to URL and extracting the path.
     * <p/>
     * URL must be absolute.
     *
     * @param url
     * @return
     */
    public static JsonApiPath parsePathFromStringUrl(String url, String apiMountPath) {
        try {
            return parsePathFromStringUrl(URI.create(url).toURL(), apiMountPath);
        } catch (MalformedURLException e) {
            throw new GenericKatharsisException("Invalid url " + url);
        }
    }


    /**
     * Parses the given path. Expects just the JSON API specific path, without the API mount path..
     *
     * @param requestPath
     * @return
     */
    protected static JsonApiPath parsePath(String requestPath, String requestQuery) {
        String[] pathParts = splitPath(requestPath.toString());

        validatePath(pathParts);

        String resource = parseResource(pathParts);

        Optional<List<String>> ids = parseIds(pathParts);
        Optional<String> relationship = relationship(pathParts);
        Optional<String> field = parseField(pathParts);
        Optional<String> query = parseQuery(requestQuery);

        return new JsonApiPath(resource, ids, relationship, field, query);

    }

    /**
     * Parses path provided by the application. The path provided cannot contain neither hostname nor protocol. It
     * can start or end with slash e.g. <i>/tasks/1/</i> or <i>tasks/1</i>.
     *
     * @param path Path to be parsed
     * @return doubly-linked list which represents path given at the input
     */

    public static JsonApiPath parsePathFromStringUrl(URL path, String apiMountPath) {
        Path requestPath = Paths.get(apiMountPath).relativize(Paths.get(path.getPath()));
        return parsePath(requestPath.toString(), path.getQuery());
    }

    private static Optional<String> parseQuery(String query) {
        return Optional.ofNullable(query);
    }

    private static void validatePath(String[] pathParts) {
        if (pathParts.length == 0) {
            throw new IllegalStateException("Path must have at leas one element");
        }

        if (pathParts.length > 4) {
            throw new IllegalStateException("Path has too many elements " + pathParts);
        }

        if (pathParts.length == 4) {
            if (!pathParts[2].equalsIgnoreCase(RELATIONSHIP_MARK)) {
                throw new IllegalStateException("No relationships mark was not found at position 3 " + pathParts);
            }
        }
    }

    private static String parseResource(String[] strings) {
        return strings[0];
    }

    private static Optional<List<String>> parseIds(String[] parts) {
        if (!hasIds(parts)) {
            return Optional.empty();
        }
        String[] idsStrings = parts[1].split(DEFAULT_ID_SEPARATOR);

        List<String> ids = new ArrayList<>();
        for (String id : idsStrings) {
            ids.add(id);
        }
        return Optional.of(ids);
    }

    private static Optional<String> parseField(String[] strings) {
        if (!hasField(strings)) {
            return Optional.empty();
        }
        return Optional.of(strings[2]);
    }

    private static Optional<String> relationship(String[] strings) {
        if (!hasRelationship(strings)) {
            return Optional.empty();
        }
        return Optional.of(strings[3]);
    }

    protected static boolean isCollection(String[] strings) {
        return (strings.length == 1) || (strings.length == 2 && strings[1].contains(DEFAULT_ID_SEPARATOR));
    }

    protected static boolean hasIds(String[] parts) {
        return parts.length >= 2;
    }

    protected static boolean hasField(String[] parts) {
        return parts.length == 3;
    }

    protected static boolean hasRelationship(String[] parts) {
        // if we have 4 elements, check for relationships mark
        return parts.length == 4 ? parts[2].equalsIgnoreCase(RELATIONSHIP_MARK) : false;
    }

    public boolean isCollection() {
        if (ids.isPresent()) {
            return ids.get().size() > 1;
        } else {
            // resource is always present, check for absence of field and relationship
            return !hasFieldOrResource();
        }
    }

    public boolean isResource() {
        return isCollection() || (isSingleResource() && !hasFieldOrResource());
    }

    private boolean isSingleResource() {
        return ids.isPresent() ? ids.get().size() == 1 : false;
    }

    private boolean hasFieldOrResource() {
        return field.isPresent() || relationship.isPresent();
    }

    public boolean isRelationshipResource() {
        return !(field.isPresent() || isCollection()) && relationship.isPresent();
    }

    public boolean isField() {
        return field.isPresent() && !(relationship.isPresent() || isCollection());
    }
}
