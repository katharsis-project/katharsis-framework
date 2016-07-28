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
package io.katharsis.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.errorhandling.mapper.KatharsisExceptionMapper;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.servlet.util.QueryStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Katharsis dispatcher invoker.
 */
public class KatharsisInvoker {

    private static Logger log = LoggerFactory.getLogger(KatharsisInvoker.class);

    private static int BUFFER_SIZE = 4096;

    private ObjectMapper objectMapper;
    private QueryParamsBuilder queryParamsBuilder;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;

    public KatharsisInvoker(ObjectMapper objectMapper, QueryParamsBuilder queryParamsBuilder,
                            ResourceRegistry resourceRegistry, RequestDispatcher requestDispatcher) {
        this.objectMapper = objectMapper;
        this.queryParamsBuilder = queryParamsBuilder;
        this.resourceRegistry = resourceRegistry;
        this.requestDispatcher = requestDispatcher;
    }

    public void invoke(KatharsisInvokerContext invokerContext) throws KatharsisInvokerException {
        if (isAcceptableMediaType(invokerContext)) {
            try {
                dispatchRequest(invokerContext);
            } catch (Exception e) {
                throw new KatharsisInvokerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
            }
        } else {
            throw new KatharsisInvokerException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
        }
    }

    private void dispatchRequest(KatharsisInvokerContext invokerContext) throws Exception {
        BaseResponseContext katharsisResponse = null;

        boolean passToMethodMatcher = false;

        InputStream in = null;

        try {
            JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath(invokerContext.getRequestPath());

            QueryParams queryParams = createQueryParams(invokerContext);

            in = invokerContext.getRequestEntityStream();
            RequestBody requestBody = inputStreamToBody(in);

            String method = invokerContext.getRequestMethod();
            RepositoryMethodParameterProvider parameterProvider = invokerContext.getParameterProvider();
            katharsisResponse = requestDispatcher.dispatchRequest(jsonPath, method, queryParams, parameterProvider,
                                                                  requestBody);
        } catch (KatharsisMappableException e) {
            if (log.isDebugEnabled()) {
                log.warn("Error occurred while dispatching katharsis request. " + e, e);
            } else {
                log.warn("Error occurred while dispatching katharsis request. " + e);
            }
            katharsisResponse = new KatharsisExceptionMapper().toErrorResponse(e);
        } catch (KatharsisMatchingException e) {
            passToMethodMatcher = true;
        } finally {
            closeQuietly(in);

            if (katharsisResponse != null) {
                invokerContext.setResponseStatus(katharsisResponse.getHttpStatus());
                invokerContext.setResponseContentType(JsonApiMediaType.APPLICATION_JSON_API);

                ByteArrayOutputStream baos = null;
                OutputStream out = null;

                try {
                    // first write to a buffer first because objectMapper may fail while writing.
                    baos = new ByteArrayOutputStream(BUFFER_SIZE);
                    objectMapper.writeValue(baos, katharsisResponse);

                    out = invokerContext.getResponseOutputStream();
                    out.write(baos.toByteArray());
                    out.flush();
                } finally {
                    closeQuietly(baos);
                    closeQuietly(out);
                }
            } else {
                invokerContext.setResponseStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        }
    }

    private boolean isAcceptableMediaType(KatharsisInvokerContext invokerContext) {
        String acceptHeader = invokerContext.getRequestHeader("Accept");

        if (acceptHeader != null) {
            String [] accepts = acceptHeader.split(",");
            MediaType acceptableType;

            for (String mediaTypeItem : accepts) {
                acceptableType = MediaType.parse(mediaTypeItem.trim());

                if (JsonApiMediaType.isCompatibleMediaType(acceptableType)) {
                    return true;
                }
            }
        }

        return false;
    }

    private QueryParams createQueryParams(KatharsisInvokerContext invokerContext) {
        Map<String, Set<String>> queryParameters =
            QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
        return this.queryParamsBuilder.buildQueryParams(queryParameters);
    }

    private RequestBody inputStreamToBody(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }

        Scanner s = new Scanner(is).useDelimiter("\\A");
        String requestBody = s.hasNext() ? s.next() : "";

        if (requestBody == null || requestBody.isEmpty()) {
            return null;
        }

        return objectMapper.readValue(requestBody, RequestBody.class);
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }
}
