package io.katharsis.queryParams;

import io.katharsis.resource.RestrictedQueryParamsMembers;

import java.util.List;
import java.util.Map;

/**
 * Builder responsible for parsing queryParams
 */
public class QueryParamsBuilder {

    public RequestParams buildRequestParams(Map<String, List<Object>> queryParams) {
        RequestParams params = new RequestParams();

        if (queryParams.containsKey(RestrictedQueryParamsMembers.filter.toString())) {
            params.setFilters(queryParams.get(RestrictedQueryParamsMembers.filter.toString()));
        }

        if (queryParams.containsKey(RestrictedQueryParamsMembers.sort.toString())) {
            params.setSorting(queryParams.get(RestrictedQueryParamsMembers.sort.toString()));
        }

        if (queryParams.containsKey(RestrictedQueryParamsMembers.group.toString())) {
            params.setGrouping(queryParams.get(RestrictedQueryParamsMembers.group.toString()));
        }

        if (queryParams.containsKey(RestrictedQueryParamsMembers.page.toString())) {
            params.setPagination(queryParams.get(RestrictedQueryParamsMembers.page.toString()));
        }

        if (queryParams.containsKey(RestrictedQueryParamsMembers.fields.toString())) {
            params.setIncludedFields(queryParams.get(RestrictedQueryParamsMembers.fields.toString()));
        }

        if (queryParams.containsKey(RestrictedQueryParamsMembers.include.toString())) {
            params.setIncludedRelations(queryParams.get(RestrictedQueryParamsMembers.include.toString()));
        }

        return params;
    }
}
