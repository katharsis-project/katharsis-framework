package io.katharsis.internal.boot;

import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.Module;
import io.katharsis.module.ServiceDiscovery;
import io.katharsis.module.SimpleModule;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;

public class KatharsisBootTest {

	@Test
	public void test() {
		KatharsisBoot boot = new KatharsisBoot();
		ObjectMapper objectMapper = boot.getObjectMapper();
		ResourceFieldNameTransformer resourceFieldNameTransformer = new ResourceFieldNameTransformer(
				objectMapper.getSerializationConfig());

		final Properties properties = new Properties();
		properties.put(KatharsisBootProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.resource.mock");
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				return (String) properties.get(key);
			}
		};

		boot.setServiceLocator(new SampleJsonServiceLocator());
		boot.setDefaultServiceUrlProvider(new ServiceUrlProvider() {

			@Override
			public String getUrl() {
				return "http://127.0.0.1";
			}
		});
		boot.setPropertiesProvider(propertiesProvider);
		boot.setResourceFieldNameTransformer(resourceFieldNameTransformer);
		boot.addModule(new SimpleModule("test"));
		boot.boot();

		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry<?> taskEntry = resourceRegistry.getEntry(Task.class);
		Assert.assertNotEquals(0, taskEntry.getRelationshipEntries().size());
		ResourceRepositoryAdapter<?, ?> repositoryAdapter = taskEntry.getResourceRepository(null);
		Assert.assertNotNull(repositoryAdapter.getResourceRepository());
		JsonApiResponse response = repositoryAdapter.findAll(new QueryParamsAdapter(new QueryParams()));
		Assert.assertNotNull(response);

		Assert.assertNotNull(requestDispatcher);

		ServiceDiscovery serviceDiscovery = boot.getServiceDiscovery();
		Assert.assertNotNull(serviceDiscovery);
		Assert.assertNotNull(boot.getModuleRegistry());

		List<Module> modules = boot.getModuleRegistry().getModules();
		Assert.assertEquals(2, modules.size());
		boot.setDefaultPageLimit(20L);
	}
}
