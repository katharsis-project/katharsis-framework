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
package com.github.woonsan.katharsis.invoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Platform / Framework neutral invoker context abstraction
 * hiding underlying request/response contexts.
 * Hopefully, this abstraction can help wider adoptions. For example,
 * <ul>
 * <li>Simple Servlet or Filter deployments</li>
 * <li>Spring Framework integration with DelegatingFilterProxy</li>
 * <li>Hierarchical aggregation frameworks such as Portlet/Portlet, Wicket, etc.</li>
 * </ul>
 */
public interface KatharsisInvokerContext {

    public String getRequestHeader(String name);

    public String getRequestPath();

    public String getRequestMethod();

    public String getRequestQueryString();

    public String [] getRequestParameterValues(String name);

    public String getRequestParameter(String name);

    public InputStream getRequestEntityStream() throws IOException;

    public void setResponseStatus(int status);

    public void setResponseContentType(String type);

    public OutputStream getResponseOutputStream() throws IOException;

}
