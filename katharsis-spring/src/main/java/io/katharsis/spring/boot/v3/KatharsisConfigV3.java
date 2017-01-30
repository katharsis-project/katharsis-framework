package io.katharsis.spring.boot.v3;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.katharsis.core.internal.boot.KatharsisBoot;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.jackson.JsonApiModuleBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.spring.ErrorHandlerFilter;
import io.katharsis.spring.KatharsisFilterV2;
import io.katharsis.spring.boot.KatharsisSpringBootProperties;
import io.katharsis.spring.internal.SpringServiceDiscovery;

/**
 * Current katharsis configuration with JSON API compliance, QuerySpec and module support. 
 * Note that there is no support for QueryParams is this version due to the lack of JSON API compatibility.
 */
@Configuration
@EnableConfigurationProperties(KatharsisSpringBootProperties.class)
public class KatharsisConfigV3 {

	@Autowired
	private KatharsisSpringBootProperties properties;

	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public SpringServiceDiscovery discovery() {
		return new SpringServiceDiscovery();
	}

	@Bean
	public KatharsisBoot katharsisBoot(SpringServiceDiscovery serviceDiscovery) {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setObjectMapper(objectMapper);
		String baseUrl = properties.getDomainName() + properties.getPathPrefix();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(baseUrl));
		boot.setServiceDiscovery(serviceDiscovery);
		boot.setDefaultPageLimit(properties.getDefaultPageLimit());
		boot.setMaxPageLimit(properties.getMaxPageLimit());
		boot.boot();
		return boot;
	}

	@Bean
	public Filter springBootSampleKatharsisFilter(KatharsisBoot boot) {
		JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
		SimpleModule parameterNamesModule = jsonApiModuleBuilder.build(boot.getResourceRegistry(), false);

		objectMapper.registerModule(parameterNamesModule);
		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
		return new KatharsisFilterV2(objectMapper, resourceRegistry, requestDispatcher, properties.getPathPrefix());
	}

	@Bean
	public Filter errorHandlerFilter(KatharsisBoot boot) {
		return new ErrorHandlerFilter(objectMapper, boot.getExceptionMapperRegistry());
	}
	
	@Bean
	public ResourceRegistry resourceRegistry(KatharsisBoot boot) {
		return boot.getResourceRegistry();
	}
	
	@Bean
	public ModuleRegistry moduleRegistry(KatharsisBoot boot) {
		return boot.getModuleRegistry();
	}
}
