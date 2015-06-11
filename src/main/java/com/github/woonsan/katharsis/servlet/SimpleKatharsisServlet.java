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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.woonsan.katharsis.invoker.KatharsisInvoker;
import com.github.woonsan.katharsis.invoker.KatharsisInvokerBuilder;

/**
 * Simple Katharsis integration servlet class.
 */
public class SimpleKatharsisServlet extends AbstractKatharsisServlet {

    private static Logger log = LoggerFactory.getLogger(SimpleKatharsisServlet.class);

    public static final String INIT_PARAM_RESOURCE_SEARCH_PACKAGE = "resourceSearchPackage";

    public static final String INIT_PARAM_RESOURCE_DEFAULT_DOMAIN = "resourceDefaultDomain";

    private String resourceSearchPackage;
    private String resourceDefaultDomain;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        resourceSearchPackage = servletConfig.getInitParameter(INIT_PARAM_RESOURCE_SEARCH_PACKAGE);
        resourceDefaultDomain = servletConfig.getInitParameter(INIT_PARAM_RESOURCE_DEFAULT_DOMAIN);
    }

    public String getResourceSearchPackage() {
        return resourceSearchPackage;
    }

    public void setResourceSearchPackage(String resourceSearchPackage) {
        this.resourceSearchPackage = resourceSearchPackage;
    }

    public String getResourceDefaultDomain() {
        return resourceDefaultDomain;
    }

    public void setResourceDefaultDomain(String resourceDefaultDomain) {
        this.resourceDefaultDomain = resourceDefaultDomain;
    }

    @Override
    protected KatharsisInvoker createKatharsisInvoker() {
        KatharsisInvoker katharsisInvoker = null;

        try {
            katharsisInvoker = new KatharsisInvokerBuilder()
                .resourceSearchPackage(getResourceSearchPackage())
                .resourceDefaultDomain(getResourceDefaultDomain())
                .build();
        } catch (Exception e) {
            log.error("Failed to create katharsis invoker.", e);
        }

        return katharsisInvoker;
    }

}
