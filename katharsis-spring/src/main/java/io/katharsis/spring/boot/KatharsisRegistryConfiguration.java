package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
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
    private ModuleRegistry moduleRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public ResourceRegistry resourceRegistry() {
        ResourceRegistryBuilder registryBuilder =
            new ResourceRegistryBuilder(serviceLocator, moduleRegistry.getResourceInformationBuilder());

        String serverUri = properties.getDomainName() + properties.getPathPrefix();
        ResourceRegistry resourceRegistry = registryBuilder.build(properties.getResourcePackage(), serverUri);
        
        // NOTE once ModuleRegistry is more widely used, it should be possible
        // to break up the cyclic dependency between ResourceRegistry and ModuleRegistry.
        moduleRegistry.init(objectMapper, resourceRegistry);
        
        return resourceRegistry;
    }

    @Bean
    public ExceptionMapperRegistry exceptionMapperRegistry() throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(properties.getResourcePackage());
    }
}
