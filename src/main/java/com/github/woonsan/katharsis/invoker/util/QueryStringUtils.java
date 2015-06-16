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
package com.github.woonsan.katharsis.invoker.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.woonsan.katharsis.invoker.KatharsisInvokerContext;

/**
 * Utility to parse HTTP QUERY_STRING.
 */
public class QueryStringUtils {

    private QueryStringUtils() {
    }

    public static Map<String, String> parseQueryStringAsSingleValueMap(KatharsisInvokerContext invokerContext) {
        Map<String, String> queryParamMap = null;

        String queryString = invokerContext.getRequestQueryString();

        if (queryString == null) {
            queryParamMap = Collections.emptyMap();
        } else {
            // keep insertion ordered map to maintain the order of the querystring when re-constructing it from a map
            queryParamMap = new LinkedHashMap<>();

            String[] paramPairs = queryString.split("&");
            String paramName = null;

            for (String paramPair : paramPairs) {
                String[] paramNameAndValue = paramPair.split("=");
                paramName = paramNameAndValue[0].trim();

                if (paramName.length() != 0) {
                    queryParamMap.put(paramName, null);
                }
            }

            for (Map.Entry<String, String> entry : queryParamMap.entrySet()) {
                entry.setValue(invokerContext.getRequestParameter(entry.getKey()));
            }
        }

        return queryParamMap;
    }

    public static Map<String, String []> parseQueryStringAsMultiValuesMap(KatharsisInvokerContext invokerContext) {
        Map<String, String []> queryParamMap = null;

        String queryString = invokerContext.getRequestQueryString();

        if (queryString == null) {
            queryParamMap = Collections.emptyMap();
        } else {
            // keep insertion ordered map to maintain the order of the querystring when re-constructing it from a map
            queryParamMap = new LinkedHashMap<>();

            String[] paramPairs = queryString.split("&");
            String paramName = null;

            for (String paramPair : paramPairs) {
                String[] paramNameAndValue = paramPair.split("=");
                paramName = paramNameAndValue[0].trim();

                if (paramName.length() != 0) {
                    queryParamMap.put(paramName, null);
                }
            }

            for (Map.Entry<String, String []> entry : queryParamMap.entrySet()) {
                entry.setValue(invokerContext.getRequestParameterValues(entry.getKey()));
            }
        }

        return queryParamMap;
    }

}
