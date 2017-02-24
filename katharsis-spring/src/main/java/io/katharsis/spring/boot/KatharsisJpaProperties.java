package io.katharsis.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for katharsis-jpa
 */
@ConfigurationProperties("katharsis.jpa")
public class KatharsisJpaProperties {

    /**
     * The katharsis-jpa query factory type to use.
     */
    private JpaQueryFactoryType queryFactory;

    /**
     * Whether to enable the katharsis jpa auto configuration.
     */
    private Boolean enabled = true;

    public enum JpaQueryFactoryType {
        /**
         * {@link io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory}
         */
        CRITERIA,
        /**
         * {@link io.katharsis.jpa.query.querydsl.QuerydslQueryFactory}
         */
        QUERYDSL,
    }
	
	public JpaQueryFactoryType getQueryFactory() {
		return queryFactory;
	}

	public void setQueryFactory(JpaQueryFactoryType queryFactory) {
		this.queryFactory = queryFactory;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}
