package io.katharsis.queryParams;

import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.*;
import io.katharsis.resource.RestrictedQueryParamsMembers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains a set of parameters passed along with the request.
 */
public class QueryParams {
    private TypedParams<FilterParams> filters;
    private TypedParams<SortingParams> sorting;
    private TypedParams<GroupingParams> grouping;
    private TypedParams<IncludedFieldsParams> includedFields;
    private TypedParams<IncludedRelationsParams> includedRelations;
    private Map<RestrictedPaginationKeys, Integer> pagination;


    /**
     * <strong>Important!</strong> Katharsis implementation differs form JSON API
     * <a href="http://jsonapi.org/format/#fetching-filtering">definition of filtering</a>
     * in order to fit standard query parameter serializing strategy and maximize effective processing of data.
     * <p/>
     * Filter params can be send with following format (Katharsis does not specify or implement any operators): <br/>
     * <strong>filter[ResourceType][property|operator]([property|operator])* = "value"</strong><br/>
     * <p/>
     * Examples of accepted sorting of resources:
     * <ul>
     * <li>GET /tasks/?filter[Task][name]=Super task</li>
     * <li>GET /tasks/?filter[Task][name]=Super task&[Task][dueDate]=2015-10-01</li>
     * <li>GET /tasks/?filter[Task][name][$startWith]=Super task</li>
     * <li>GET /tasks/?filter[Task][name][][$startWith]=Super&[Task][name][][$endWith]=task</li>
     * </ul>
     *
     * @return {@link TypedParams} Map of filtering params passed to a request grouped by type of resource
     */
    public TypedParams<FilterParams> getFilters() {
        return filters;
    }

    void setFilters(Map<String, Set<String>> filters) {
        Map<String, Set<String>> decodedFilters = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : filters.entrySet()) {

            List<String> propertyList = buildPropertyListFromEntry(entry, RestrictedQueryParamsMembers.filter.name());
            String entryKey = String.join(".", propertyList);

            decodedFilters.put(entryKey, Collections.unmodifiableSet(entry.getValue()));
        }

        this.filters = Collections.unmodifiableMap(decodedFilters);
    }

    /**
     * <strong>Important!</strong> Katharsis implementation differs form JSON API
     * <a href="http://jsonapi.org/format/#fetching-sorting">definition of sorting</a>
     * in order to fit standard query parameter serializing strategy and maximize effective processing of data.
     * <p/>
     * Sort params can be send with following format: <br/>
     * <strong>sort[ResourceType][property]([property])* = "asc|desc"</strong>
     * <p/>
     * Examples of accepted sorting of resources:
     * <ul>
     * <li>GET /tasks/?sort[Task][name]=asc</li>
     * <li>GET /project/?sort[Project][shortName]=desc&sort[User][name][firstName]=asc&include[Project]=team</li>
     * </ul>
     *
     * @return {@link TypedParams} Map of sorting params passed to request grouped by type of resource
     */
    public TypedParams<SortingParams> getSorting() {
        return sorting;
    }

    void setSorting(Map<String, Set<String>> sorting) {
        Map<String, RestrictedSortingValues> decodedSorting = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : sorting.entrySet()) {

            List<String> propertyList = buildPropertyListFromEntry(entry, RestrictedQueryParamsMembers.sort.name());

            String entryKey = String.join(".", propertyList);

            decodedSorting.put(entryKey, RestrictedSortingValues.valueOf(entry.getValue()
                .iterator()
                .next()));
        }

        this.sorting = Collections.unmodifiableMap(decodedSorting);
    }

    /**
     * <strong>Important: </strong> Grouping itself is not specified by JSON API itself, but the
     * keyword and format it reserved for today and future use in Katharsis.
     * <p/>
     * Group params can be send with following format: <br/>
     * <strong>group[ResourceType] = "property(.property)*"</strong>
     * <p/>
     * Examples of accepted grouping of resources:
     * <ul>
     * <li>GET /tasks/?group[Task]=name</li>
     * <li>GET /project/?group[User]=name.firstName&include[Project]=team</li>
     * </ul>
     *
     * @return {@link Map} Map of grouping params passed to request grouped by type of resource
     */
    public TypedParams<GroupingParams> getGrouping() {
        return grouping;
    }

    void setGrouping(Map<String, Set<String>> grouping) {
        List<String> decodedGrouping = new LinkedList<>();

        for (Map.Entry<String, Set<String>> entry : grouping.entrySet()) {
            List<String> propertyList = buildPropertyListFromEntry(entry, RestrictedQueryParamsMembers.group.name());

            if (propertyList.size() > 0) {
                throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'group' parameter " +
                    "(0)");
            }

            String entryKey = propertyList.iterator()
                .next();

            decodedGrouping.add(entryKey);
        }
        this.grouping = Collections.unmodifiableList(decodedGrouping);
    }

    /**
     * <strong>Important!</strong> Katharsis implementation sets on strategy of pagination whereas JSON API
     * <a href="http://jsonapi.org/format/#fetching-pagination">definition of pagination</a>
     * is agnostic about pagination strategies.
     * <p/>
     * Pagination params can be send with following format: <br/>
     * <strong>page[offset|limit] = "value"</strong>
     * <p/>
     * Examples of accepted grouping of resources:
     * <ul>
     * <li>GET /projects/?page[offset]=0&page[limit]=10</li>
     * </ul>
     *
     * @return {@link Map} Map of pagination keys passed to request
     */
    public Map<RestrictedPaginationKeys, Integer> getPagination() {
        return pagination;
    }

    void setPagination(Map<String, Set<String>> pagination) {
        Map<RestrictedPaginationKeys, Integer> decodedPagination = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : pagination.entrySet()) {
            List<String> propertyList = buildPropertyListFromEntry(entry, RestrictedQueryParamsMembers.page.name());

            if (propertyList.size() > 1) {
                throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'page' parameter " +
                    "(1)");
            }

            String entryKey = propertyList.iterator()
                .next();

            decodedPagination.put(RestrictedPaginationKeys.valueOf(entryKey), Integer.valueOf(entry.getValue()
                .iterator()
                .next()));
        }

        this.pagination = Collections.unmodifiableMap(decodedPagination);

    }

    /**
     * <strong>Important!</strong> Katharsis implementation differs form JSON API
     * <a href="http://jsonapi.org/format/#fetching-sparse-fieldsets">definition of sparse field set</a>
     * in order to fit standard query parameter serializing strategy and maximize effective processing of data.
     * <p/>
     * Sparse field set params can be send with following format: <br/>
     * <strong>fields[ResourceType] = "property(.property)*"</strong><br/>
     * <p/>
     * Examples of accepted sparse field sets of resources:
     * <ul>
     * <li>GET /tasks/?fields[Task]=name</li>
     * <li>GET /tasks/?fields[Task][]=name&fields[Task][]=dueDate</li>
     * <li>GET /tasks/?fields[User]=name.surname&include[Task]=author</li>
     * </ul>
     *
     * @return {@link TypedParams} Map of sparse field set params passed to a request grouped by type of resource
     */
    public TypedParams<IncludedFieldsParams> getIncludedFields() {
        return includedFields;
    }

    void setIncludedFields(Map<String, Set<String>> sparse) {
        Map<String, Set<String>> decodedSparse = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : sparse.entrySet()) {
            List<String> propertyList = buildPropertyListFromEntry(entry, RestrictedQueryParamsMembers.fields.name());

            if (propertyList.size() > 1) {
                throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'fields' " +
                    "parameter (1)");
            }

            String entryKey = propertyList.iterator()
                .next();

            decodedSparse.put(entryKey, Collections.unmodifiableSet(entry.getValue()));
        }

        this.includedFields = Collections.unmodifiableMap(decodedSparse);
    }

    /**
     * <strong>Important!</strong> Katharsis implementation differs form JSON API
     * <a href="http://jsonapi.org/format/#fetching-includes">definition of includes</a>
     * in order to fit standard query parameter serializing strategy and maximize effective processing of data.
     * <p/>
     * Sparse field set params can be send with following format: <br/>
     * <strong>include[ResourceType] = "property(.property)*"</strong><br/>
     * <p/>
     * Examples of accepted sparse field sets of resources:
     * <ul>
     * <li>GET /tasks/?include[Task]=author</li>
     * <li>GET /tasks/?include[Task][]=author&include[Task][]=comments</li>
     * <li>GET /projects/?include[Project]=task&include[Task]=comments</li>
     * </ul>
     *
     * @return {@link TypedParams} Map of sparse field set params passed to a request grouped by type of resource
     */
    public TypedParams<IncludedRelationsParams> getIncludedRelations() {
        return includedRelations;
    }

    void setIncludedRelations(Map<String, Set<String>> inclusions) {
        List<Inclusion> decodedInclusions = new LinkedList<>();

        for (Map.Entry<String, Set<String>> entry : inclusions.entrySet()) {
            List<String> propertyList = buildPropertyListFromEntry(entry, RestrictedQueryParamsMembers.include.name());

            if (propertyList.size() > 0) {
                throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'include' " +
                    "parameter (0)");
            }

            decodedInclusions.add(new Inclusion(entry.getValue()
                .iterator()
                .next()));
        }

        this.includedRelations = Collections.unmodifiableList(decodedInclusions);
    }

    private List<String> buildPropertyListFromEntry(Map.Entry<String, Set<String>> entry, String prefix) {
        String entryKey = entry.getKey()
            .substring(prefix.length());

        String pattern = "\\w+(?<!\\[)(?=\\])";
        Pattern regexp = Pattern.compile(pattern);
        Matcher matcher = regexp.matcher(entryKey);
        List<String> matchList = new ArrayList<>();

        while (matcher.find()) {
            matchList.add(matcher.group());
        }

        return matchList;
    }

}
