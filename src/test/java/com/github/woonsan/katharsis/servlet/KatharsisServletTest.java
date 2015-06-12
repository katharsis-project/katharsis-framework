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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import com.github.woonsan.katharsis.invoker.JsonApiMediaType;

/**
 * Test for {@link AbstractKatharsisServlet}.
 */
public class KatharsisServletTest {

    private static Logger log = LoggerFactory.getLogger(KatharsisServletTest.class);

    private static final String RESOURCE_SEARCH_PACKAGE = "com.github.woonsan.katharsis.resource";

    private static final String RESOURCE_DEFAULT_DOMAIN = "http://test.local";

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    private HttpServlet katharsisServlet;

    @Before
    public void before() throws Exception {
        katharsisServlet = new SampleKatharsisServlet();

        servletContext = new MockServletContext();
        ((MockServletContext) servletContext).setContextPath("");
        servletConfig = new MockServletConfig(servletContext);
        ((MockServletConfig) servletConfig).addInitParameter(SampleKatharsisFilter.INIT_PARAM_RESOURCE_SEARCH_PACKAGE,
                                                             RESOURCE_SEARCH_PACKAGE);
        ((MockServletConfig) servletConfig).addInitParameter(SampleKatharsisFilter.INIT_PARAM_RESOURCE_DEFAULT_DOMAIN,
                                                             RESOURCE_DEFAULT_DOMAIN);

        katharsisServlet.init(servletConfig);
    }

    @After
    public void after() throws Exception {
        katharsisServlet.destroy();
    }

    @Test
    public void onSimpleCollectionGetShouldReturnCollectionOfResources() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks/");
        request.setRequestURI("/api/tasks/");
        request.setContentType(JsonApiMediaType.APPLICATION_JSON_API);
        request.addHeader("Accept", "*/*");

        MockHttpServletResponse response = new MockHttpServletResponse();

        katharsisServlet.service(request, response);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonEquals("{\"data\":[{\"data\":{\"id\":1,\"name\":\"First task\",\"project\":null}}]}", responseContent);
    }

    @Test
    public void onSimpleResourceGetShouldReturnOneResource() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks/1");
        request.setRequestURI("/api/tasks/1");
        request.setContentType(JsonApiMediaType.APPLICATION_JSON_API);
        request.addHeader("Accept", "*/*");
        request.addParameter("filter", "");

        MockHttpServletResponse response = new MockHttpServletResponse();

        katharsisServlet.service(request, response);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonEquals("{\"data\":{\"data\":{\"id\":1,\"name\":\"Some task\",\"project\":null}}}", responseContent);
    }

    @Test
    public void onCollectionRequestWithParamsGetShouldReturnCollection() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setMethod("GET");
        request.setContextPath("");
        request.setServletPath("/api");
        request.setPathInfo("/tasks");
        request.setRequestURI("/api/tasks");
        request.setContentType(JsonApiMediaType.APPLICATION_JSON_API);
        request.addHeader("Accept", "*/*");
        request.addParameter("filter", "{\"name\":\"John\"}");

        MockHttpServletResponse response = new MockHttpServletResponse();

        katharsisServlet.service(request, response);

        String responseContent = response.getContentAsString();

        log.debug("responseContent: {}", responseContent);
        assertNotNull(responseContent);

        assertJsonEquals("{\"data\":[{\"data\":{\"id\":1,\"name\":\"First task\",\"project\":null}}]}", responseContent);
    }

}
