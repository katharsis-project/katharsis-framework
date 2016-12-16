package io.katharsis.spring.boot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for katharsis-jpa
 */
@ConfigurationProperties("katharsis.jpa")
@Getter
@Setter
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
}
