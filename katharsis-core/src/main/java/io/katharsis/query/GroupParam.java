package io.katharsis.query;

import lombok.NonNull;
import lombok.Value;

import java.util.Map;
import java.util.Set;

/**
 * <strong>Important: </strong> Grouping itself is not specified by JSON API itself, but the
 * keyword and format it reserved for today and future use in Katharsis.
 * <p/>
 * Group params can be send with following format: <br>
 * <strong>group[ResourceType] = "property(.property)*"</strong>
 * <p/>
 * Examples of accepted grouping of resources:
 * <ul>
 * <li>{@code GET /tasks/?group[tasks]=name}</li>
 * <li>{@code GET /project/?group[users]=name.firstName&include[projects]=team}</li>
 * </ul>
 *
 * @return {@link Map} Map of grouping params passed to request grouped by type of resource
 */
@Value
public class GroupParam {

    public static final String PREFIX = "group";

    private String qualifier;
    private Set<String> values;

    public static GroupParam build(@NonNull String param, @NonNull Set<String> values) {
        String qualifier = param.substring(PREFIX.length());
        return new GroupParam(qualifier, values);
    }

    @Override
    public String toString() {
        return PREFIX + qualifier + "=" + values;
    }

}
