package io.katharsis.spring.boot.autoconfigure;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.spring.boot.KatharsisJpaProperties;
import io.katharsis.spring.boot.KatharsisSpringBootProperties;
import io.katharsis.spring.boot.v3.KatharsisConfigV3;
import io.katharsis.spring.jpa.SpringTransactionRunner;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Katharsis' JPA module.
 * <p>
 * Activates when there is a bean of type {@link javax.persistence.EntityManagerFactory} and
 * {@link javax.persistence.EntityManager} on the classpath and there is no other existing
 * {@link io.katharsis.jpa.JpaModule} configured.
 * <p>
 * Disable with the property <code>katharsis.jpa.enabled = false</code>
 * <p>
 * This configuration class will activate <em>after</em> the Hibernate auto-configuration.
 */
@Configuration
@ConditionalOnBean({EntityManager.class, EntityManagerFactory.class})
@ConditionalOnClass(JpaModule.class)
@ConditionalOnProperty(prefix = "katharsis.jpa", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(JpaModule.class)
@EnableConfigurationProperties({KatharsisJpaProperties.class, KatharsisSpringBootProperties.class})
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@AutoConfigureBefore
@Import({ KatharsisConfigV3.class})
public class KatharsisJpaAutoConfiguration {

    @Autowired
    private EntityManager em;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private KatharsisJpaProperties jpaProperties;

    @Bean
    public SpringTransactionRunner transactionRunner() {
        return new SpringTransactionRunner();
    }

    @Bean
    public JpaModule jpaModule() {
        JpaModule module = JpaModule.newServerModule(emf, em, transactionRunner());

        if (jpaProperties.getQueryFactory() != null) {
            switch (jpaProperties.getQueryFactory()) {
                case CRITERIA:
                    module.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
                    break;
                case QUERYDSL:
                    module.setQueryFactory(QuerydslQueryFactory.newInstance());
                    break;
            }
        }
        return module;
    }
}
