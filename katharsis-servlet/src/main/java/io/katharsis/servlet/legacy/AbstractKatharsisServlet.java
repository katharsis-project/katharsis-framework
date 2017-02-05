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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.invoker.internal.KatharsisInvokerContext;
import io.katharsis.invoker.internal.KatharsisInvokerException;
import io.katharsis.invoker.internal.legacy.KatharsisInvoker;
import io.katharsis.invoker.internal.legacy.KatharsisInvokerBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.servlet.internal.ServletKatharsisInvokerContext;

/**
 * Abstract base servlet class to integrate with Katharsis-core.
 * <p>
 * Child class can override {@link #createKatharsisInvoker()} method with
 * proper {@link ObjectMapper}, {@link ResourceRegistry} and {@link RequestDispatcher}.
 * </p>
 */
abstract public class AbstractKatharsisServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(AbstractKatharsisServlet.class);

    private volatile KatharsisInvoker katharsisInvoker;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        KatharsisInvokerContext invokerContext = createKatharsisInvokerContext(request, response);

        try {
            getKatharsisInvoker().invoke(invokerContext);
        } catch (KatharsisInvokerException e) {
            log.warn("Katharsis Invoker exception.", e);
            response.setStatus(e.getStatusCode());
        } catch (Exception e) {
            throw new ServletException("Katharsis invocation failed.", e);
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

    protected KatharsisInvokerContext createKatharsisInvokerContext(HttpServletRequest request, HttpServletResponse response) {
        return new ServletKatharsisInvokerContext(getServletContext(), request, response);
    }

    protected KatharsisInvoker createKatharsisInvoker() throws Exception {
        return createKatharsisInvokerBuilder().build();
    }

    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();
}
