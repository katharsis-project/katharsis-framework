package io.katharsis.queryParams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.resource.RestrictedQueryParamsMembers;

import java.io.IOException;
import java.util.Map;

/**
 * Builder responsible for parsing queryParams. The created {@link RequestParams} object contains several fields
 * where each of them is not-null only when this parameter has been passed with a request.
 */
public class RequestParamsBuilder {

    private final ObjectMapper objectMapper;

    public RequestParamsBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Filters and groups query params
     *
     * @param queryParams Map of provided query params
     * @return RequestParams containing filtered query params grouped by JSON:API standard
     * @throws ParametersDeserializationException thrown when unsupported input format is detected
     */
    public RequestParams buildRequestParams(Map<String, String> queryParams) {
        RequestParams requestParams = new RequestParams(objectMapper);

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
            throw new ParametersDeserializationException(e.getMessage());
        }

        return requestParams;
    }
}
