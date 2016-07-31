package io.katharsis.query;

import lombok.NonNull;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

/**
 * <strong>Important!</strong> Katharsis implementation differs form JSON API
 * <a href="http://jsonapi.org/format/#fetching-includes">definition of includes</a>
 * in order to fit standard query parameter serializing strategy and maximize effective processing of data.
 * <p/>
 * Included field set params can be send with following format: <br>
 * <strong>include[ResourceType] = "property(.property)*"</strong><br>
 * <p/>
 * Examples of accepted sparse field sets of resources:
 * <ul>
 * <li>{@code GET /tasks/?include[tasks]=author}</li>
 * <li>{@code GET /tasks/?include[tasks][]=author&include[tasks][]=comments}</li>
 * <li>{@code GET /projects/?include[projects]=task&include[tasks]=comments}</li>
 * </ul>
 * <p/>
 * References:
 * <p/>
 * http://jsonapi.org/format/#fetching-includes
 */
@Value
public class IncludeParam {

    public static final String PREFIX = "include";

    //TODO: ieugen: this is used in Katharsis but it is not defined by JSON-API spec
    private String qualifier;

    /**
     * The value of the include parameter MUST be a comma-separated (U+002C COMMA, “,”) list of relationship paths.
     * A relationship path is a dot-separated (U+002E FULL-STOP, “.”) list of relationship names.
     */
    private List<String> paths;

    public static IncludeParam build(@NonNull String paramName, @NonNull String paramValue) {
        String qualifier = paramName.substring(PREFIX.length());

        return new IncludeParam(qualifier, Arrays.asList(paramValue.split(",")));
    }

    @Override
    public String toString() {
        return PREFIX + qualifier + "=" + paths;
    }
}
