package io.katharsis.rs;

/**
 * Katharsis configuration properties.
 */
public class KatharsisProperties {

    /**
     * Set package to scan for resources.
     *
     * It allows configuring from which package should be searched to get models and repositories used by the core.
     * Only one package can be specified.
     *
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 1.0.0
     */
    public static final String RESOURCE_SEARCH_PACKAGE = "katharsis.config.core.resource.package";

    /**
     * Set default domain.
     *
     * An URL assigned to this value will be added to all of the links returned by Katharsis framework.
     *
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 1.0.0
     */
    public static final String RESOURCE_DEFAULT_DOMAIN = "katharsis.config.core.resource.domain";
}
