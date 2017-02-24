package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.exception.ExceptionMapperRegistryBuilder;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.spring.legacy.SpringServiceLocator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
    public ResourceRegistry resourceRegistry(ServiceUrlProvider serviceUrlProvider) {
        ResourceRegistryBuilder registryBuilder =
                new ResourceRegistryBuilder(moduleRegistry, serviceLocator, moduleRegistry.getResourceInformationBuilder());

        ResourceRegistry resourceRegistry = registryBuilder.build(properties.getResourcePackage(), moduleRegistry, serviceUrlProvider);

        // NOTE once ModuleRegistry is more widely used, it should be possible
        // to break up the cyclic dependency between ResourceRegistry and ModuleRegistry.
        moduleRegistry.init(objectMapper);

        return resourceRegistry;
    }

    @Bean
    public ExceptionMapperRegistry exceptionMapperRegistry() throws Exception {
        ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
        return mapperRegistryBuilder.build(properties.getResourcePackage());
    }


    @Bean
    public ServiceUrlProvider getServiceUrlProvider() {
        return new ServiceUrlProvider() {

            @Value("${katharsis.pathPrefix}")
            private String pathPrefix;

            @Resource
            private HttpServletRequest request;

            @Override
            public String getUrl() {
                String scheme = request.getScheme();
                String host = request.getHeader("host");
                return scheme + "://" + host + pathPrefix;
            }
        };
    }
}
