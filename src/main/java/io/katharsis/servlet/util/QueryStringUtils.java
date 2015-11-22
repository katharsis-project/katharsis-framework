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
package io.katharsis.servlet.util;

import io.katharsis.invoker.KatharsisInvokerContext;
import io.katharsis.jackson.exception.ParametersDeserializationException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility to parse HTTP QUERY_STRING.
 */
public class QueryStringUtils {

    private static final String QUERY_CHARSET = StandardCharsets.UTF_8.name();

    private QueryStringUtils() {
    }

    public static Map<String, Set<String>> parseQueryStringAsSingleValueMap(KatharsisInvokerContext invokerContext) {
        try {
            return buildSingleQueryParams(invokerContext);
        } catch (UnsupportedEncodingException e) {
            throw new ParametersDeserializationException("Couldn't decode param: " + e.getMessage());
        }
    }

    private static Map<String, Set<String>> buildSingleQueryParams(KatharsisInvokerContext invokerContext) throws
        UnsupportedEncodingException {
        Map<String, Set<String>> queryParamMap;
        String queryString = invokerContext.getRequestQueryString();

        if (queryString == null) {
            queryParamMap = Collections.emptyMap();
        } else {
            // keep insertion ordered map to maintain the order of the query string when re-constructing it from a map
            queryParamMap = new LinkedHashMap<>();

            String[] paramPairs = queryString.split("&");
            String paramName;

            for (String paramPair : paramPairs) {
                String[] paramNameAndValue = paramPair.split("=");

                if (paramNameAndValue.length > 1) {
                    paramName = decode(paramNameAndValue[0], invokerContext.isUrlDecoded());
                    queryParamMap.put(paramName, null);
                }
            }

            for (Map.Entry<String, Set<String>> entry : queryParamMap.entrySet()) {
                String queryParameter = invokerContext.getQueryParameter(encode(entry.getKey(), invokerContext.isUrlDecoded()));
                if (queryParameter != null) {
                    String decodedValue = decode(queryParameter, invokerContext.isUrlDecoded());
                    entry.setValue(Collections.singleton(decodedValue));
                }
            }
        }
        return queryParamMap;
    }

    public static Map<String, String[]> parseQueryStringAsMultiValuesMap(KatharsisInvokerContext invokerContext) {
        try {
            return buildMultiQueryParams(invokerContext);
        } catch (UnsupportedEncodingException e) {
            throw new ParametersDeserializationException("Couldn't decode param: " + e.getMessage());
        }
    }

    private static Map<String, String[]> buildMultiQueryParams(KatharsisInvokerContext invokerContext) throws
        UnsupportedEncodingException {
        Map<String, String[]> queryParamMap;
        String queryString = invokerContext.getRequestQueryString();

        if (queryString == null) {
            queryParamMap = Collections.emptyMap();
        } else {
            // keep insertion ordered map to maintain the order of the query string when re-constructing it from a map
            queryParamMap = new LinkedHashMap<>();

            String[] paramPairs = queryString.split("&");
            String paramName;

            for (String paramPair : paramPairs) {
                String[] paramNameAndValue = paramPair.split("=");

                if (paramNameAndValue.length > 1) {
                    paramName = decode(paramNameAndValue[0].trim(), invokerContext.isUrlDecoded());
                    if (paramName.length() != 0) {
                        queryParamMap.put(paramName, null);
                    }
                }
            }

            for (Map.Entry<String, String[]> entry : queryParamMap.entrySet()) {
                List<String> decodedParameterValueList = new LinkedList<>();
                for (String parameterValue : invokerContext.getQueryParameterValues(encode(entry.getKey(), invokerContext.isUrlDecoded()))) {
                    if (parameterValue != null) {
                        String decodedValue = decode(parameterValue, invokerContext.isUrlDecoded());
                        decodedParameterValueList.add(decodedValue);
                    }
                }
                entry.setValue(decodedParameterValueList.toArray(new String[decodedParameterValueList.size()]));
            }
        }
        return queryParamMap;
    }

    private static String encode(String key, boolean urlDecoded) throws UnsupportedEncodingException {
        if (!urlDecoded) {
            return URLEncoder.encode(key,QUERY_CHARSET);
        } else {
            return key;
        }
    }

    private static String decode(String key, boolean urlDecoded) throws UnsupportedEncodingException {
        if (!urlDecoded) {
            return URLDecoder.decode(key, QUERY_CHARSET);
        } else {
            return key;
        }
    }
}
