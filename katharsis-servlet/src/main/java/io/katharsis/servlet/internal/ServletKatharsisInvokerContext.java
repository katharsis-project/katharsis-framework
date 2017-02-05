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
package io.katharsis.servlet.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.katharsis.invoker.internal.KatharsisInvokerContext;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;

/**
 * Servlet API based {@link KatharsisInvokerContext} implementation.
 */
public class ServletKatharsisInvokerContext implements KatharsisInvokerContext {

    private final ServletContext servletContext;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public ServletKatharsisInvokerContext(final ServletContext servletContext, final HttpServletRequest request,
                                          final HttpServletResponse response) {
        this.servletContext = servletContext;
        this.request = request;
        this.response = response;
    }

    @Override
	public String getRequestHeader(String name) {
        return request.getHeader(name);
    }

    @Override
	public String getRequestPath() {
        String path = request.getPathInfo();

        // Serving with Filter, pathInfo can be null.
        if (path == null) {
            path = request.getRequestURI().substring(request.getContextPath().length());
        }

        return path;
    }

    @Override
	public String getRequestMethod() {
        return request.getMethod();
    }

    @Override
	public String getRequestQueryString() {
        return request.getQueryString();
    }

    @Override
	public String [] getQueryParameterValues(String name) {
        return request.getParameterValues(name);
    }

    @Override
	public String getQueryParameter(String name) {
        return request.getParameter(name);
    }

    @Override
	public InputStream getRequestEntityStream() throws IOException {
        return request.getInputStream();
    }

    @Override
	public void setResponseStatus(int sc) {
        response.setStatus(sc);
    }

    @Override
	public void setResponseContentType(String type) {
        response.setContentType(type);
    }

    @Override
	public OutputStream getResponseOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
	public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
	public HttpServletRequest getServletRequest() {
        return request;
    }

    @Override
	public HttpServletResponse getServletResponse() {
        return response;
    }

    @Override
    public RepositoryMethodParameterProvider getParameterProvider() {
        return new ServletParametersProvider(servletContext, request, response);
    }
}
