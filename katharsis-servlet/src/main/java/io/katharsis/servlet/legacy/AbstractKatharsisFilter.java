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
package io.katharsis.servlet.legacy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.invoker.internal.KatharsisInvokerContext;
import io.katharsis.invoker.internal.KatharsisInvokerException;
import io.katharsis.invoker.internal.legacy.KatharsisInvoker;
import io.katharsis.invoker.internal.legacy.KatharsisInvokerBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.servlet.internal.ServletKatharsisInvokerContext;

/**
 * Abstract base servlet filter class to integrate with Katharsis-core.
 * <p>
 * Child class can override {@link #createKatharsisInvoker()} method with
 * proper {@link ObjectMapper}, {@link ResourceRegistry} and {@link RequestDispatcher}.
 * </p>
 * <p>
 * If you want to deploy a filter, type of this, in a specific prefix path,
 * then you should configure an init-parameter, "filterBasePath", with the prefix.
 * </p>
 * <p>
 * In Spring Framework based web application, you might want to inject a {@link KatharsisInvoker}
 * bean using {@link #setKatharsisInvoker(KatharsisInvoker)} method
 * if you can use <code>org.springframework.web.filter.DelegatingFilterProxy</code>.
 * </p>
 */
abstract public class AbstractKatharsisFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(AbstractKatharsisFilter.class);

    private ServletContext servletContext;
    private volatile KatharsisInvoker katharsisInvoker;

    private String filterBasePath;

    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
        filterBasePath = filterConfig.getInitParameter(KatharsisProperties.WEB_PATH_PREFIX);
    }

    @Override
	public void destroy() {
    }

    @Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {

        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            req.setCharacterEncoding("UTF-8");

            KatharsisInvokerContext invokerContext = createKatharsisInvokerContext(request, response);

            try {
                getKatharsisInvoker().invoke(invokerContext);
            } catch (KatharsisInvokerException e) {
                log.warn("Katharsis Invoker exception.", e);
                response.setStatus(e.getStatusCode());
            } catch (Exception e) {
                throw new ServletException("Katharsis invocation failed.", e);
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    public KatharsisInvoker getKatharsisInvoker() throws Exception {
        // Double-checked locking..
        KatharsisInvoker invoker = katharsisInvoker;

        if (invoker == null) {
            synchronized (this) {
                invoker = katharsisInvoker;

                if (invoker == null) {
                    invoker = createKatharsisInvoker();
                    katharsisInvoker = invoker;
                }
            }
        }

        return invoker;
    }

    public void setKatharsisInvoker(KatharsisInvoker katharsisInvoker) {
        this.katharsisInvoker = katharsisInvoker;
    }

    public String getFilterBasePath() {
        return filterBasePath;
    }

    public void setFilterBasePath(String filterBasePath) {
        this.filterBasePath = filterBasePath;
    }

    protected ServletContext getServletContext() {
        return servletContext;
    }

    protected KatharsisInvokerContext createKatharsisInvokerContext(HttpServletRequest request, HttpServletResponse response) {
        return new ServletKatharsisInvokerContext(getServletContext(), request, response) {
            @Override
            public String getRequestPath() {
                String path = super.getRequestPath();

                if (filterBasePath != null && path.startsWith(filterBasePath)) {
                    path = path.substring(filterBasePath.length());
                }

                return path;
            }

        };
    }

    protected KatharsisInvoker createKatharsisInvoker() throws Exception {
        return createKatharsisInvokerBuilder().build();
    }

    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();

}
