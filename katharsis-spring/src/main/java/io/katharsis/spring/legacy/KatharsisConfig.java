package io.katharsis.spring.legacy;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.katharsis.invoker.internal.legacy.KatharsisInvokerBuilder;
import io.katharsis.spring.boot.KatharsisSpringBootProperties;

/**
 * {@link KatharsisConfigV2} should be used instead
 */
@Deprecated
@Configuration
@EnableConfigurationProperties(KatharsisSpringBootProperties.class)
public class KatharsisConfig {

    @Autowired
    private KatharsisSpringBootProperties properties;

    @Bean
    public Filter springBootSampleKatharsisFilter() {
        KatharsisFilter filter = new KatharsisFilter();
        filter.setResourceSearchPackage(properties.getResourcePackage());
        filter.setResourceDomain(properties.getDomainName());
        filter.setPathPrefix(properties.getPathPrefix());
        return filter;
    }

    @Bean
    public KatharsisInvokerBuilder katharsisInvokerBuilder() {
        return new KatharsisInvokerBuilder();
    }
}