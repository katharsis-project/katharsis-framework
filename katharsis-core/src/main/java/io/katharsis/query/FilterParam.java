package io.katharsis.query;

import io.katharsis.queryParams.params.TypedParams;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;


/**
 * <strong>Important!</strong> Katharsis implementation differs form JSON API
 * <a href="http://jsonapi.org/format/#fetching-filtering">definition of filtering</a>
 * in order to fit standard query parameter serializing strategy and maximize effective processing of data.
 * <p/>
 * Filter params can be send with following format (Katharsis does not specify or implement any operators): <br>
 * <strong>filter[ResourceType][property|operator]([property|operator])* = "value"</strong><br>
 * <p/>
 * Examples of accepted filtering of resources:
 * <ul>
 * <li>{@code GET /tasks/?filter[tasks][name]=Super task}</li>
 * <li>{@code GET /tasks/?filter[tasks][name]=Super task&filter[tasks][dueDate]=2015-10-01}</li>
 * <li>{@code GET /tasks/?filter[tasks][name][$startWith]=Super task}</li>
 * <li>{@code GET /tasks/?filter[tasks][name][][$startWith]=Super&filter[tasks][name][][$endWith]=task}</li>
 * </ul>
 *
 * @return {@link TypedParams} Map of filtering params passed to a request grouped by type of resource
 */
@Value
public class FilterParam {

    public static final String PREFIX = "filter";

    private String qualifier;
    private Set<String> values;

    public static FilterParam build(@NonNull String param, @NonNull Set<String> values) {
        String qualifier = param.substring(PREFIX.length());
        return new FilterParam(qualifier, values);
    }

    @Override
    public String toString() {
        return PREFIX + qualifier + "=" + values;
    }
}
