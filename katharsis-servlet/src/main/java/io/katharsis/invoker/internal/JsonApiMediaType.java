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
package io.katharsis.invoker.internal;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;

public class JsonApiMediaType {

    private static Logger log = LoggerFactory.getLogger(JsonApiMediaType.class);

    /**
     * A {@code String} constant representing {@value #APPLICATION_JSON_API} media type.
     */
    public final static String APPLICATION_JSON_API = "application/vnd.api+json;charset=UTF-8";

    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_JSON_API} media type.
     */
    public static final MediaType APPLICATION_JSON_API_TYPE = MediaType
        .create("application", "vnd.api+json")
        .withCharset(StandardCharsets.UTF_8);

    private static final String WILDCARD = "*";

    public static boolean isCompatibleMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }

        if (WILDCARD.equals(mediaType.type())) {
            return true;
        }

        if (MediaType.ANY_APPLICATION_TYPE.type()
            .equalsIgnoreCase(mediaType.type())) {
            log.debug("application mediaType : {}", mediaType);
            if (WILDCARD.equals(mediaType.subtype())) {
                return true;
            }

            if (APPLICATION_JSON_API_TYPE.subtype()
                .equalsIgnoreCase(mediaType.subtype())) {
                log.debug("application mediaType having json api subtype : {}", mediaType);
                return true;
            }
        }

        return false;
    }

    private JsonApiMediaType() {
    }

}
