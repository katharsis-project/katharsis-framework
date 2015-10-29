package io.katharsis.queryParams;

import io.katharsis.errorhandling.exception.KatharsisException;
import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.resource.RestrictedQueryParamsMembers;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builder responsible for parsing queryParams. The created {@link QueryParams} object contains several fields
 * where each of them is not-null only when this parameter has been passed with a request.
 */
public class QueryParamsBuilder {

    /**
     * Decodes passed query paramaeters
     *
     * @param queryParams Map of provided query params
     * @return QueryParams containing filtered query params grouped by JSON:API standard
     * @throws ParametersDeserializationException thrown when unsupported input format is detected
     */
    public QueryParams buildQueryParams(Map<String, Set<String>> queryParams) {
        QueryParams deserializedQueryParams = new QueryParams();

        try {
            String filterKey = RestrictedQueryParamsMembers.filter.name();
            Map<String, Set<String>> filterQueryParams = filterQueryParamsByKey(queryParams, filterKey);
            deserializedQueryParams.setFilters(filterQueryParams);

            String sortingKey = RestrictedQueryParamsMembers.sort.name();
            Map<String, Set<String>> sortingQueryParams = filterQueryParamsByKey(queryParams, sortingKey);
            deserializedQueryParams.setSorting(sortingQueryParams);

            String groupingKey = RestrictedQueryParamsMembers.group.name();
            Map<String, Set<String>> groupingQueryParams = filterQueryParamsByKey(queryParams, groupingKey);
            deserializedQueryParams.setGrouping(groupingQueryParams);

            String pagingKey = RestrictedQueryParamsMembers.page.name();
            Map<String, Set<String>> pagingQueryParams = filterQueryParamsByKey(queryParams, pagingKey);
            deserializedQueryParams.setPagination(pagingQueryParams);

            String sparseKey = RestrictedQueryParamsMembers.fields.name();
            Map<String, Set<String>> sparseQueryParams = filterQueryParamsByKey(queryParams, sparseKey);
            deserializedQueryParams.setIncludedFields(sparseQueryParams);

            String includeKey = RestrictedQueryParamsMembers.include.name();
            Map<String, Set<String>> includeQueryParams = filterQueryParamsByKey(queryParams, includeKey);
            deserializedQueryParams.setIncludedRelations(includeQueryParams);

        } catch (RuntimeException e) {
            if (e instanceof KatharsisException) {
                throw e;
            } else {
                throw new ParametersDeserializationException(e.getMessage());
            }
        }

        return deserializedQueryParams;
    }

    /**
     * Filters provided query params to one starting with provided string key
     *
     * @param queryParams Request query params
     * @param queryKey    Filtering key
     * @return Filtered query params
     */
    private Map<String, Set<String>> filterQueryParamsByKey(Map<String, Set<String>> queryParams, String queryKey) {
        return queryParams.entrySet()
            .stream()
            .filter(p -> p.getKey()
                .startsWith(queryKey))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
