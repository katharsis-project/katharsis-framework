package io.katharsis.query;

import io.katharsis.errorhandling.exception.QueryParseException;
import io.katharsis.queryParams.PaginationKey;
import lombok.NonNull;
import lombok.Value;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <strong>Important!</strong> Katharsis implementation sets on strategy of pagination whereas JSON API
 * <a href="http://jsonapi.org/format/#fetching-pagination">definition of pagination</a>
 * is agnostic about pagination strategies.
 * <p/>
 * Pagination params can be send with following format: <br>
 * <strong>page[offset|limit|number|size|cursor] = "value"</strong>
 * <p/>
 * Examples of accepted grouping of resources:
 * <ul>
 * <li>{@code GET /projects/?page[offset]=0&page[limit]=10}</li>
 * <li>{@code GET /projects/?page[number]=4&page[size]=10}</li>
 * <li>{@code GET /projects/?page[cursor]=opaquestring}</li>
 * </ul>
 *
 * @return {@link Map} Map of pagination keys passed to request
 */
@Value
public class PaginationParam {

    public static final String PREFIX = "page";

    /**
     * Match a pagination of the form fields[TYPE] to extract the TYPE which is the resource name.
     */
    public static final Pattern PAGINATION_PATTERN = Pattern.compile("(page)\\[(number|size|offset|limit|cursor)\\]", Pattern.CASE_INSENSITIVE);

    private PaginationKey paginationKey;
    private String value;

    public static PaginationParam build(@NonNull String queryParam, @NonNull Collection<String> queryParamValues) {
        Matcher matcher = PAGINATION_PATTERN.matcher(queryParam);

        if (matcher.matches()) {
            String paginationKey = matcher.group(2).toLowerCase();
            return new PaginationParam(PaginationKey.valueOf(paginationKey), getFirstValue(queryParam, queryParamValues));
        } else {
            throw new IllegalStateException("Provided fields parameter does not match JSON-API spec field[TYPE]" + queryParam);
        }
    }

    private static String getFirstValue(String queryParam, Collection<String> queryParams) {
        for (String first : queryParams) {
            return first;
        }
        throw new QueryParseException("Pagination query param has no value " + queryParam);
    }

    public Integer getAsInt() {
        return Integer.parseInt(value);
    }

    public Long getAsLong() {
        return Long.parseLong(value);
    }

    @Override
    public String toString() {
        return PREFIX + paginationKey + "=" + value;
    }
}
