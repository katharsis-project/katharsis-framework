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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import io.katharsis.invoker.internal.KatharsisInvokerException;

public class KatharsisInvokerExceptionTest {

    @Test
    public void testExceptions() throws Exception {
        KatharsisInvokerException ex = new KatharsisInvokerException(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(ex instanceof RuntimeException);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getStatusCode());
        assertNull(ex.getCause());
        assertNull(ex.getMessage());
        assertNotNull(ex.toString());

        ex = new KatharsisInvokerException(HttpServletResponse.SC_BAD_REQUEST, "Invocation failed.");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getStatusCode());
        assertNull(ex.getCause());
        assertEquals("Invocation failed.", ex.getMessage());
        assertNotNull(ex.toString());

        ex = new KatharsisInvokerException(HttpServletResponse.SC_BAD_REQUEST, new Exception("Root cause."));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getStatusCode());
        assertNotNull(ex.getCause());
        assertEquals("Root cause.", ex.getCause().getMessage());
        assertNotNull(ex.toString());

        ex = new KatharsisInvokerException(HttpServletResponse.SC_BAD_REQUEST, "Invocation failed.", new Exception("Root cause."));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getStatusCode());
        assertNotNull(ex.getCause());
        assertEquals("Root cause.", ex.getCause().getMessage());
        assertEquals("Invocation failed.", ex.getMessage());
        assertNotNull(ex.toString());
    }
}
