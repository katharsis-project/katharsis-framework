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

/**
 * Katharsis configuration properties.
 */
public final class KatharsisProperties {

    /**
     * Set package to scan for resources, repositories and exception mappers.
     *<p>
     * It allows configuring from which package should be searched to get models, repositories used by the core and
     * exception mappers used to map thrown from repositories exceptions.
     *</p>
     * <p>
     * Multiple packages can be passed by specifying a comma separated string of packages.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 0.9.4
     */
    public static final String RESOURCE_SEARCH_PACKAGE = "katharsis.config.core.resource.package";

    /**
     * Set default domain.
     * <p>
     * An URL assigned to this value will be added to all of the links returned by Katharsis framework. The URL
     * cannot end with slash.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 0.9.4
     */
    public static final String RESOURCE_DEFAULT_DOMAIN = "katharsis.config.core.resource.domain";

    /**
     * Set prefix to be searched when performing method matching and building building <i>links</i> objects in
     * responses.
     *
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 0.9.4
     */
    public static final String WEB_PATH_PREFIX = "katharsis.config.web.path.prefix";
}
