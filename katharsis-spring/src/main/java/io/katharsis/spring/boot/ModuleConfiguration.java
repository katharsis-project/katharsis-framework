package io.katharsis.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.module.CoreModule;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.information.ResourceFieldNameTransformer;

@Configuration
public class ModuleConfiguration {

	@Autowired
    private KatharsisSpringBootProperties properties;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Bean
	public ModuleRegistry moduleRegistry() {
		ResourceFieldNameTransformer resourceFieldNameTransformer = new ResourceFieldNameTransformer(
				objectMapper.getSerializationConfig());
		ModuleRegistry registry = new ModuleRegistry();
		String resourceSearchPackage = properties.getResourcePackage();
		registry.addModule(new CoreModule(resourceSearchPackage, resourceFieldNameTransformer));
		return registry;
	}
}