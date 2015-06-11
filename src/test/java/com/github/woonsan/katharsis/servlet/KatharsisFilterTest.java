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
package com.github.woonsan.katharsis.servlet;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertNotNull;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import com.github.woonsan.katharsis.invoker.JsonApiMediaType;
import com.github.woonsan.katharsis.invoker.KatharsisInvoker;
import com.github.woonsan.katharsis.invoker.KatharsisInvokerBuilder;

/**
 * Test for {@link AbstractKatharsisFilter}.
 */
public class KatharsisFilterTest {

    private static Logger log = LoggerFactory.getLogger(KatharsisFilterTest.class);

    private static final String RESOURCE_SEARCH_PACKAGE = "com.github.woonsan.katharsis.resource";

    private static final String RESOURCE_DEFAULT_DOMAIN = "http://test.local";

    private KatharsisInvoker katharsisInvoker;

    private ServletContext servletContext;

    private FilterConfig filterConfig;

    private Filter katharsisFilter;

    @Before
    public void before() throws Exception {
        katharsisInvoker = new KatharsisInvokerBuilder()
            .resourceSearchPackage(RESOURCE_SEARCH_PACKAGE)
            .resourceDefaultDomain(RESOURCE_DEFAULT_DOMAIN)
            .build();

        katharsisFilter = new AbstractKatharsisFilter() {
            @Override
            protected KatharsisInvoker createKatharsisInvoker() {
                return katharsisInvoker;
            }
        };

        servletContext = new MockServletContext();
        ((MockServletContext) servletContext).setContextPath("");
        filterConfig = new MockFilterConfig(servletContext);
        ((MockFilterConfig) filterConfig).addInitParameter("filterBasePath", "/api");

        katharsisFilter.init(filterConfig);
    }

    @After
    public void after() throws Exception {
        katharsisFilter.destroy();
    }

    @Test
    public void onSimpleCollectionGetShouldReturnCollectionOfResources() throws Exception {
        MockFilterChain filterChain = new MockFilterChain();

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath(null);
        request.setPathInfo(null);
        request.setRequestURI("/api/tasks/");
        request.setContentType(JsonApiMediaType.APPLICATION_JSON_API);
        request.addHeader("Accept", "*/*");

        MockHttpServletResponse response = new MockHttpServletResponse();

        katharsisFilter.doFilter(request, response, filterChain);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonEquals("{\"data\":[{\"data\":{\"id\":1,\"name\":\"First task\",\"project\":null}}]}", responseContent);
    }

    @Test
    public void onSimpleResourceGetShouldReturnOneResource() throws Exception {
        MockFilterChain filterChain = new MockFilterChain();

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath(null);
        request.setPathInfo(null);
        request.setRequestURI("/api/tasks/1");
        request.setContentType(JsonApiMediaType.APPLICATION_JSON_API);
        request.addHeader("Accept", "*/*");
        request.addParameter("filter", "");

        MockHttpServletResponse response = new MockHttpServletResponse();

        katharsisFilter.doFilter(request, response, filterChain);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonEquals("{\"data\":{\"data\":{\"id\":1,\"name\":\"Some task\",\"project\":null}}}", responseContent);
    }

    @Test
    public void onCollectionRequestWithParamsGetShouldReturnCollection() throws Exception {
        MockFilterChain filterChain = new MockFilterChain();

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath(null);
        request.setPathInfo(null);
        request.setRequestURI("/api/tasks");
        request.setContentType(JsonApiMediaType.APPLICATION_JSON_API);
        request.addHeader("Accept", "*/*");
        request.addParameter("filter", "{\"name\":\"John\"}");

        MockHttpServletResponse response = new MockHttpServletResponse();

        katharsisFilter.doFilter(request, response, filterChain);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonEquals("{\"data\":[{\"data\":{\"id\":1,\"name\":\"First task\",\"project\":null}}]}", responseContent);
    }

}
