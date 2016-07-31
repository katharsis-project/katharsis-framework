package io.katharsis.query;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A server MAY choose to support requests to sort resource collections according to one or more criteria (“sort fields”).
 * <p/>
 * Note: Although recommended, sort fields do not necessarily need to correspond to resource attribute and association names.
 * <p/>
 * Reference:
 * <p/>
 * http://jsonapi.org/format/#fetching-sorting
 */
@RequiredArgsConstructor
public class SortingParam implements Iterable<Map.Entry<String, Boolean>> {

    public static final boolean ASCENDING = true;
    public static final boolean DESCENDING = false;

    public static final String PREFIX = "sort";
    // order of keys is important
    private final LinkedHashMap<String, Boolean> values;

    public static SortingParam build(@NonNull Collection<String> queryParamValues) {
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();

        for (String queryParamValue : queryParamValues) {
            for (String sortField : queryParamValue.split(",")) {
                if (sortField.charAt(0) == '-') {
                    sortFields.put(sortField.substring(1), DESCENDING);
                } else {
                    sortFields.put(sortField, ASCENDING);
                }
            }
        }
        return new SortingParam(sortFields);
    }

    public Set<String> sortFields() {
        return values.keySet();
    }

    public Set<Map.Entry<String, Boolean>> sortEntries() {
        return values.entrySet();
    }

    public Boolean isAscending(@NonNull String fieldName) {
        return values.get(fieldName);
    }

    @Override
    public String toString() {
        return PREFIX + "=" + values;
    }

    @Override
    public Iterator<Map.Entry<String, Boolean>> iterator() {
        return values.entrySet().iterator();
    }

}
