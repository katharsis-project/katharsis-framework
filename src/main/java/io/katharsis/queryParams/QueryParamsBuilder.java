package io.katharsis.queryParams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.exception.JsonDeserializationException;
import io.katharsis.resource.RestrictedQueryParamsMembers;

import java.io.IOException;
import java.util.Map;

/**
 * Builder responsible for parsing queryParams
 */
public class QueryParamsBuilder {

    /**
     * Filters and groups query params
     *
     * @param queryParams Map of provided query params
     * @return RequestParams containing filtered query params grouped by JSON:API standard
     */
    public RequestParams buildRequestParams(Map<String, String> queryParams) throws JsonDeserializationException {
        RequestParams requestParams = new RequestParams(new ObjectMapper());

        try {
            String filterKey = RestrictedQueryParamsMembers.filter.name();
            if (queryParams.containsKey(filterKey)) {
                requestParams.setFilters(queryParams.get(filterKey));
            }

            String sortingKey = RestrictedQueryParamsMembers.sort.name();
            if (queryParams.containsKey(sortingKey)) {
                requestParams.setSorting(queryParams.get(sortingKey));
            }

            String groupingKey = RestrictedQueryParamsMembers.group.name();
            if (queryParams.containsKey(groupingKey)) {
                requestParams.setGrouping(queryParams.get(groupingKey));
            }

            String pagingKey = RestrictedQueryParamsMembers.page.name();
            if (queryParams.containsKey(pagingKey)) {
                requestParams.setPagination(queryParams.get(pagingKey));
            }

            String fieldsKey = RestrictedQueryParamsMembers.fields.name();
            if (queryParams.containsKey(fieldsKey)) {
                requestParams.setIncludedFields(queryParams.get(fieldsKey));
            }

            String includeKey = RestrictedQueryParamsMembers.include.name();
            if (queryParams.containsKey(includeKey)) {
                requestParams.setIncludedRelations(queryParams.get(includeKey));
            }
        } catch (IOException e) {
            throw new JsonDeserializationException("Exception while reading request parameters", e);
        }

        return requestParams;
    }
}
