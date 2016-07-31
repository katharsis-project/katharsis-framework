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
import io.katharsis.dispatcher.JsonApiDispatcher;
import io.katharsis.dispatcher.ResponseContext;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Katharsis dispatcher invoker.
 */
@Slf4j
public class KatharsisInvoker {

    private static int BUFFER_SIZE = 4096;

    private ObjectMapper objectMapper;
    private JsonApiDispatcher requestDispatcher;
    private String apiMountPath;

    public KatharsisInvoker(@NonNull ObjectMapper objectMapper,
                            @NonNull JsonApiDispatcher requestDispatcher,
                            @NonNull String apiMountPath) {
        this.objectMapper = objectMapper;
        this.requestDispatcher = requestDispatcher;
        this.apiMountPath = apiMountPath;
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
        ResponseContext katharsisResponse = null;
        InputStream in = null;

        try {
            in = invokerContext.getRequestEntityStream();

            String method = invokerContext.getRequestMethod();
            RepositoryMethodParameterProvider parameterProvider = invokerContext.getParameterProvider();

            JsonApiPath path = JsonApiPath.parsePathFromStringUrl(invokerContext.getRequestPath(), apiMountPath);
            Request request = new Request(path, method, in, parameterProvider);

            katharsisResponse = requestDispatcher.handle(request);

        } catch (KatharsisMatchingException e) {
            //TODO: ieugen: handle exception
            log.error("Exception {}", e);
        } catch (Exception ex) {
            //TODO: ieugen: handle exception
            log.error("Exception {}", ex);
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
            String[] accepts = acceptHeader.split(",");
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

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }
}
