package io.katharsis.spring.boot;

import io.katharsis.dispatcher.registry.DefaultRepositoryRegistry;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KatharsisSpringBootProperties.class)
public class KatharsisRegistryConfiguration {

    @Autowired
    private KatharsisSpringBootProperties properties;

    @Bean
    public RepositoryRegistry repositoryRegistry() {
        String serverUri = properties.getDomainName() + properties.getPathPrefix();
        return DefaultRepositoryRegistry.build(properties.getResourcePackage(), serverUri);
    }

    @Bean
    public ExceptionMapperRegistry exceptionMapperRegistry() throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(properties.getResourcePackage());
    }
}
