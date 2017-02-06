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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.net.MediaType;

import io.katharsis.invoker.internal.JsonApiMediaType;

public class JsonApiMediaTypeTest {

    @Test
    public void testIsCompatibleMediaType() throws Exception {
        assertFalse(JsonApiMediaType.isCompatibleMediaType(null));
        assertTrue(JsonApiMediaType.isCompatibleMediaType(MediaType.parse(JsonApiMediaType.APPLICATION_JSON_API)));
        assertTrue(JsonApiMediaType.isCompatibleMediaType(MediaType.parse("application/vnd.api+json; name=data.xyz")));
        assertTrue(JsonApiMediaType.isCompatibleMediaType(MediaType.ANY_TYPE));
        assertTrue(JsonApiMediaType.isCompatibleMediaType(MediaType.ANY_APPLICATION_TYPE));
        assertFalse(JsonApiMediaType.isCompatibleMediaType(MediaType.ANY_TEXT_TYPE));
    }

}
