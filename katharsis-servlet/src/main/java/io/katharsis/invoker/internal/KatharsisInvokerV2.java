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
package io.katharsis.invoker.internal;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;

import io.katharsis.core.internal.boot.KatharsisBoot;
import io.katharsis.core.internal.boot.PropertiesProvider;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.core.internal.exception.KatharsisExceptionMapper;
import io.katharsis.errorhandling.exception.JsonDeserializationException;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.module.Module;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.servlet.internal.QueryStringUtils;

/**
 * Katharsis dispatcher invoker.
 */
public class KatharsisInvokerV2 {

	private static int BUFFER_SIZE = 4096;

	private KatharsisBoot boot = new KatharsisBoot();

	/**
	 * Sets a custom ServiceUrlProvider.
	 * 
	 * @param serviceUrlProvider
	 */
	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		boot.setServiceUrlProvider(serviceUrlProvider);
	}

	/**
	 * Sets a custom ServiceUrlProvider.
	 * 
	 * @param serviceUrlProvider
	 */
	public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
		boot.setPropertiesProvider(propertiesProvider);
	}

	public void addModule(Module module) {
		boot.addModule(module);
	}

	public ObjectMapper getObjectMapper() {
		return boot.getObjectMapper();
	}

	public void setDefaultPageLimit(Long defaultPageLimit) {
		boot.setDefaultPageLimit(defaultPageLimit);
	}

	public QuerySpecDeserializer getQuerySpecDeserializer() {
		return boot.getQuerySpecDeserializer();
	}

	public KatharsisBoot getBoot() {
		return boot;
	}

	public void configure() {
		boot.boot();
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
		Response katharsisResponse = null;

		boolean passToMethodMatcher = false;

		InputStream in = null;

		try {
			ResourceRegistry resourceRegistry = boot.getResourceRegistry();
			RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
			JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath(invokerContext.getRequestPath());

			Map<String, Set<String>> parameters = getParameters(invokerContext);

			in = invokerContext.getRequestEntityStream();
			Document requestBody = inputStreamToBody(in);

			String method = invokerContext.getRequestMethod();
			RepositoryMethodParameterProvider parameterProvider = invokerContext.getParameterProvider();
			katharsisResponse = requestDispatcher.dispatchRequest(jsonPath, method, parameters, parameterProvider, requestBody);
		} catch (KatharsisMappableException e) {
			// log error in KatharsisMappableException mapper.
			katharsisResponse = new KatharsisExceptionMapper().toErrorResponse(e).toResponse();
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
					// first write to a buffer first because objectMapper may
					// fail while writing.
					baos = new ByteArrayOutputStream(BUFFER_SIZE);
					ObjectMapper objectMapper = boot.getObjectMapper();
					objectMapper.writeValue(baos, katharsisResponse.getDocument());

					out = invokerContext.getResponseOutputStream();
					out.write(baos.toByteArray());
					out.flush();
				} finally {
					closeQuietly(baos);
					closeQuietly(out);
				}
			} else if (passToMethodMatcher) {
				invokerContext.setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
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

	private Map<String, Set<String>> getParameters(KatharsisInvokerContext invokerContext) {
		return QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
	}

	private Document inputStreamToBody(InputStream is) throws IOException {
		if (is == null) {
			return null;
		}

		Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
		String requestBody = s.hasNext() ? s.next() : "";

		if (requestBody == null || requestBody.isEmpty()) {
			return null;
		}

		try {
			ObjectMapper objectMapper = boot.getObjectMapper();
			return objectMapper.readValue(requestBody, Document.class);
		} catch (IOException e) {
			throw new JsonDeserializationException(e.getMessage());
		}
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
