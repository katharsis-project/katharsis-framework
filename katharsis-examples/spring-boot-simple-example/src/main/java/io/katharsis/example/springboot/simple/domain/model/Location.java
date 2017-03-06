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
package io.katharsis.example.springboot.simple.domain.model;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;

import java.util.UUID;

@JsonApiResource(type = "location")
public class Location {

    private static long startId = 0;

    @JsonApiId
    private Long id;

    private String name;

    public Location() {
        id = startId++;
        name = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
