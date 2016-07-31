package io.katharsis.query;

import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A client MAY request that an endpoint return only specific fields in the response on a per-type basis
 * by including a fields[TYPE] parameter.
 * <p/>
 * The value of the fields parameter MUST be a comma-separated (U+002C COMMA, “,”) list that refers to the name(s)
 * of the fields to be returned.
 * <p/>
 * If a client requests a restricted set of fields for a given resource type, an endpoint MUST NOT include additional
 * fields in resource objects of that type in its response.
 *
 *
 * GET /articles?include=author&fields[articles]=title,body&fields[people]=name HTTP/1.1
 *
 * Examples of accepted sparse field sets of resources:
 * <ul>
 * <li>{@code GET /tasks/?fields[tasks]=name}</li>
 * <li>{@code GET /tasks/?fields[tasks][]=name&fields[tasks][]=dueDate}</li>
 * <li>{@code GET /tasks/?fields[users]=name.surname&include[tasks]=author}</li>
 * </ul>
 * <p/>
 * <p/>
 * Reference:
 * <p/>
 * http://jsonapi.org/format/#fetching-sparse-fieldsets
 */
@Value
public class FieldsParam {

    public static final String PREFIX = "fields";

    /**
     * Match a field of the form fields[TYPE] to extract the TYPE which is the resource name.
     */
    public static final Pattern FIELDS_PATTERN = Pattern.compile("(fields)\\[(.*)\\]", Pattern.CASE_INSENSITIVE);

    private String type;
    private List<String> fieldNames;

    public static FieldsParam build(@NonNull String param, @NonNull Collection<String> queryParamValues) {
        Matcher matcher = FIELDS_PATTERN.matcher(param);

        if (matcher.matches()) {
            String fieldType = matcher.group(2);
            return new FieldsParam(fieldType, extractFieldNames(queryParamValues));
        } else {
            throw new IllegalStateException("Provided fields parameter does not match JSON-API spec field[TYPE]" + param);
        }
    }

    private static List<String> extractFieldNames(@NonNull Collection<String> queryParamValues) {
        List<String> fieldNames = new ArrayList<>();
        for (String paramValue : queryParamValues) {
            for (String fieldName : paramValue.split(",")) {
                fieldNames.add(fieldName);
            }
        }
        return fieldNames;
    }

    @Override
    public String toString() {
        return PREFIX + type + "=" + fieldNames;
    }
}
