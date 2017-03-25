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
	 * There are two mechanisms in place to determine whether an inclusion was
	 * requested.
	 * 
	 * <ul>
	 * <li>include[tasks]=project.schedule</li> (BY_TYPE)
	 * <li>include[tasks]=project&include[projects]=schedule</li> (BY_ROOT_PATH)
	 * </ul>
	 * 
	 * For simple object structures they are semantically the same, but they do differ
	 * for more complex ones, like when multiple attributes lead
	 * to the same type or for cycle structures. In the later case BY_TYPE inclusions 
	 * become recursive, while BY_ROOT_PATH do not. Note that the use of BY_TYPE
	 * outmatches BY_ROOT_PATH, so BY_TYPE includes everything BY_ROOT_PATH does
	 * and potentially more.
	 * 
	 * Possible values: BY_TYPE (default), BY_ROOT_PATH 
	 */
    public static final String INCLUDE_BEHAVIOR = "katharsis.config.include.behavior";

    /**
     * Set a boolean whether katharsis will try to overwrite a value previously assigned
     * to a relationship field that has been included in the request.
     * Refer to {@link io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically}.overwrite for only adding it to a specific field.
     *
     * @since 2.8.2
     */
    public static final String INCLUDE_AUTOMATICALLY_OVERWRITE = "katharsis.config.include.automatically.overwrite";
    
    /**
     * See {@link ResourceFieldImmutableWriteBehavior}. By default
     * {@value ResourceFieldImmutableWriteBehavior#IGNORE} is used.
     */
    public static final String RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR = "katharsis.config.resource.immutableWrite";
}
