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
package io.katharsis.servlet;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonPartEquals;
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

import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.invoker.internal.JsonApiMediaType;
import io.katharsis.servlet.internal.ServletUrlProvider;
import io.katharsis.servlet.resource.repository.NodeRepository;

/**
 * Test for {@link ServletUrlProvider}. Proper urls should be set in response
 * despite any lack of configuration.
 */
public class ServletUrlProviderTest {

	private static final String RESOURCE_SEARCH_PACKAGE = "io.katharsis.servlet.resource";

	private static final String FIRST_TASK_LINKS = "{\"self\":\"http://localhost:80/api/tasks/1\"}";

	private static Logger log = LoggerFactory.getLogger(ServletUrlProviderTest.class);

	private ServletContext servletContext;

	private ServletConfig servletConfig;

	private HttpServlet katharsisServlet;

	private NodeRepository nodeRepository;

	@Before
	public void before() throws Exception {
		katharsisServlet = new KatharsisServlet();

		servletContext = new MockServletContext();
		((MockServletContext) servletContext).setContextPath("");
		servletConfig = new MockServletConfig(servletContext);
		((MockServletConfig) servletConfig).addInitParameter(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, RESOURCE_SEARCH_PACKAGE);

		katharsisServlet.init(servletConfig);
		nodeRepository = new NodeRepository();
	}

	@After
	public void after() throws Exception {
		katharsisServlet.destroy();
		nodeRepository.clearRepo();
	}

	@Test
	public void testServiceUrl() throws Exception {
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
		assertJsonPartEquals("tasks", responseContent, "data[0].type");
		assertJsonPartEquals("\"1\"", responseContent, "data[0].id");

		// check url
		assertJsonPartEquals(FIRST_TASK_LINKS, responseContent, "data[0].links");
	}
}
