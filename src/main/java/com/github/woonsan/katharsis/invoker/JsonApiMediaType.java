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

import com.google.common.net.MediaType;

public class JsonApiMediaType {

    /**
     * A {@code String} constant representing {@value #APPLICATION_JSON_API} media type.
     */
    public final static String APPLICATION_JSON_API = "application/vnd.api+json";

    /**
     * A {@link MediaType} constant representing {@value #APPLICATION_JSON_API} media type.
     */
    public static final MediaType APPLICATION_JSON_API_TYPE = MediaType.create("application", "vnd.api+json");

    private static final String ALL_MEDIA_TYPE = "*/*";

    private static final String WILDCARD = "*";

    public static boolean isCompatibleMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }

        if (ALL_MEDIA_TYPE.equals(mediaType)) {
            return true;
        }

        return WILDCARD.equals(mediaType.type()) ||
            (APPLICATION_JSON_API_TYPE.type().equalsIgnoreCase(mediaType.type()) &&
                (WILDCARD.equals(mediaType.subtype()) || APPLICATION_JSON_API_TYPE.subtype().equalsIgnoreCase(mediaType.subtype())));
    }

    private JsonApiMediaType() {
    }

}
