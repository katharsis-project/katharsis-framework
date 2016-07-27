/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.katharsis.queryParams;

import io.katharsis.resource.RestrictedQueryParamsMembers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultQueryParamsParser implements QueryParamsParser {

    @Override
    public Map<String, Set<String>> parseFiltersParameters(final Map<String, Set<String>> queryParams) {
        String filterKey = RestrictedQueryParamsMembers.filter.name();
        return filterQueryParamsByKey(queryParams, filterKey);
    }

    @Override
    public Map<String, Set<String>> parseSortingParameters(final Map<String, Set<String>> queryParams) {
        String sortingKey = RestrictedQueryParamsMembers.sort.name();
        return filterQueryParamsByKey(queryParams, sortingKey);
    }

    @Override
    public Map<String, Set<String>> parseGroupingParameters(final Map<String, Set<String>> queryParams) {
        String groupingKey = RestrictedQueryParamsMembers.group.name();
        return filterQueryParamsByKey(queryParams, groupingKey);
    }

    @Override
    public Map<String, Set<String>> parseIncludedFieldsParameters(final Map<String, Set<String>> queryParams) {
        String sparseKey = RestrictedQueryParamsMembers.fields.name();
        return filterQueryParamsByKey(queryParams, sparseKey);
    }

    @Override
    public Map<String, Set<String>> parseIncludedRelationsParameters(final Map<String, Set<String>> queryParams) {
        String includeKey = RestrictedQueryParamsMembers.include.name();
        return filterQueryParamsByKey(queryParams, includeKey);
    }

    @Override
    public Map<String, Set<String>> parsePaginationParameters(final Map<String, Set<String>> queryParams) {
        String pagingKey = RestrictedQueryParamsMembers.page.name();
        return filterQueryParamsByKey(queryParams, pagingKey);
    }

    /**
     * Filters provided query params to one starting with provided string key
     *
     * @param queryParams Request query params
     * @param queryKey    Filtering key
     * @return Filtered query params
     */
    private static Map<String, Set<String>> filterQueryParamsByKey(Map<String, Set<String>> queryParams, String queryKey) {
        Map<String, Set<String>> filteredQueryParams = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : queryParams.entrySet()) {
            if (entry.getKey().startsWith(queryKey)) {
                filteredQueryParams.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredQueryParams;
    }
}
