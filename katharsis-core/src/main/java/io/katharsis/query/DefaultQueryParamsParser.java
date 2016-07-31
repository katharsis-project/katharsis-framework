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

package io.katharsis.query;

import io.katharsis.errorhandling.exception.QueryParseException;
import io.katharsis.queryParams.QueryParamsParser;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import lombok.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DefaultQueryParamsParser implements QueryParamsParser {

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

    /**
     * Code adapted from http://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, Set<String>> splitQuery(String query) {
        final Map<String, Set<String>> query_pairs = new LinkedHashMap<>();
        final String[] pairs = extractQueryKeyValuePairs(query);
        try {
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedHashSet<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.get(key).add(value);
            }
            return query_pairs;
        } catch (UnsupportedEncodingException e) {
            throw new QueryParseException(String.format("Could not parse query %s. %s", query, e.getMessage()));
        }
    }

    private static String[] extractQueryKeyValuePairs(String query) {
        return query != null ? query.split("&") : new String[]{};
    }

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

    public QueryParams parse(@NonNull String query) {
        return new QueryParams(splitQueryParams(query));
    }

    public Map<String, Set<String>> splitQueryParams(@NonNull String query) {
        return splitQuery(query);
    }

}
