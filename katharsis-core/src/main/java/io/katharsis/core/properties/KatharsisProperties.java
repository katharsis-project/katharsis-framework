package io.katharsis.core.properties;


public class KatharsisProperties {

    /**
     * Set package to scan for resources, repositories and exception mappers.
     * <p>
     * It allows configuring from which package should be searched to get models, repositories used by the core and
     * exception mappers used to map thrown from repositories exceptions.
     * </p>
     * <p>
     * Multiple packages can be passed by specifying a comma separated string of packages.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 0.9.0
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
     * @since 0.9.0
     */
    public static final String RESOURCE_DEFAULT_DOMAIN = "katharsis.config.core.resource.domain";

    /**
     * Set prefix to be searched when performing method matching and building building <i>links</i> objects in
     * responses.
     * <p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 0.9.4
     */
    public static final String WEB_PATH_PREFIX = "katharsis.config.web.path.prefix";

    /**
     * Set a boolean whether katharsis will always try to look up a relationship field that has been included in the request.
     * Refer to {@link io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically} for only adding it to a specific field.
     *
     * @since 2.8.2
     */
    public static final String INCLUDE_AUTOMATICALLY = "katharsis.config.include.automatically";

    /**
     * Set a boolean whether katharsis will try to overwrite a value previously assigned
     * to a relationship field that has been included in the request.
     * Refer to {@link io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically}.overwrite for only adding it to a specific field.
     *
     * @since 2.8.2
     */
    public static final String INCLUDE_AUTOMATICALLY_OVERWRITE = "katharsis.config.include.automatically.overwrite";
}
