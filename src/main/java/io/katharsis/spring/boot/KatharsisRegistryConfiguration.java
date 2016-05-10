package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.spring.SpringServiceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KatharsisSpringBootProperties.class)
public class KatharsisRegistryConfiguration {

    @Autowired
    private KatharsisSpringBootProperties properties;

    @Autowired
    private SpringServiceLocator serviceLocator;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public ResourceRegistry resourceRegistry() {
        ResourceRegistryBuilder registryBuilder =
            new ResourceRegistryBuilder(serviceLocator,
                new ResourceInformationBuilder(new ResourceFieldNameTransformer(objectMapper.getSerializationConfig())));

        String serverUri = properties.getDomainName() + properties.getPathPrefix();
        return registryBuilder.build(properties.getResourcePackage(), serverUri);
    }

    @Bean
    public ExceptionMapperRegistry exceptionMapperRegistry() throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(properties.getResourcePackage());
    }
}
