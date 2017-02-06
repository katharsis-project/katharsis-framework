package io.katharsis.core.internal.boot;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.boot.KatharsisBoot;
import io.katharsis.core.internal.boot.PropertiesProvider;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.query.QueryAdapterBuilder;
import io.katharsis.core.internal.query.QuerySpecAdapterBuilder;
import io.katharsis.core.internal.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.internal.QueryParamsAdapterBuilder;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.module.Module;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.module.ServiceDiscovery;
import io.katharsis.module.ServiceDiscoveryFactory;
import io.katharsis.module.SimpleModule;
import io.katharsis.queryspec.QuerySpecDeserializer;
import io.katharsis.repository.filter.DocumentFilter;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;

public class KatharsisBootTest {

	private ServiceDiscoveryFactory serviceDiscoveryFactory;

	private ServiceDiscovery serviceDiscovery;

	@Before
	public void setup() {
		serviceDiscoveryFactory = Mockito.mock(ServiceDiscoveryFactory.class);
		serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
		Mockito.when(serviceDiscoveryFactory.getInstance()).thenReturn(serviceDiscovery);
	}

	@Test
	public void setObjectMapper() {
		KatharsisBoot boot = new KatharsisBoot();
		ObjectMapper mapper = new ObjectMapper();
		boot.setObjectMapper(mapper);
		Assert.assertSame(mapper, boot.getObjectMapper());
	}

	@Test
	public void setServiceDiscovery() {
		KatharsisBoot boot = new KatharsisBoot();
		ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
		boot.setServiceDiscovery(serviceDiscovery);
		Assert.assertSame(serviceDiscovery, boot.getServiceDiscovery());
	}

	@Test
	public void setServiceDiscoveryFactory() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));
		boot.boot();
		Mockito.verify(serviceDiscoveryFactory, Mockito.times(1)).getInstance();
		Assert.assertNotNull(boot.getServiceDiscovery());
	}

	@Test
	public void setQuerySpecDeserializer() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));

		QuerySpecDeserializer deserializer = Mockito.mock(QuerySpecDeserializer.class);
		boot.setQuerySpecDeserializer(deserializer);
		Assert.assertSame(deserializer, boot.getQuerySpecDeserializer());
		boot.boot();

		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
		QueryAdapterBuilder queryAdapterBuilder = requestDispatcher.getQueryAdapterBuilder();
		Assert.assertTrue(queryAdapterBuilder instanceof QuerySpecAdapterBuilder);
	}

	@Test
	public void setQueryParamsBuilder() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));

		QueryParamsBuilder deserializer = Mockito.mock(QueryParamsBuilder.class);
		boot.setQueryParamsBuilds(deserializer);
		boot.boot();

		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
		QueryAdapterBuilder queryAdapterBuilder = requestDispatcher.getQueryAdapterBuilder();
		Assert.assertTrue(queryAdapterBuilder instanceof QueryParamsAdapterBuilder);
	}

	@Test
	public void testServiceDiscovery() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));

		Module module = Mockito.mock(Module.class);
		DocumentFilter filter = Mockito.mock(DocumentFilter.class);
		JsonApiExceptionMapper exceptionMapper = Mockito.mock(JsonApiExceptionMapper.class);
		Mockito.when(serviceDiscovery.getInstancesByType(Mockito.eq(DocumentFilter.class))).thenReturn(Arrays.asList(filter));
		Mockito.when(serviceDiscovery.getInstancesByType(Mockito.eq(Module.class))).thenReturn(Arrays.asList(module));
		Mockito.when(serviceDiscovery.getInstancesByType(Mockito.eq(JsonApiExceptionMapper.class)))
				.thenReturn(Arrays.asList(exceptionMapper));
		boot.boot();

		ModuleRegistry moduleRegistry = boot.getModuleRegistry();
		Assert.assertTrue(moduleRegistry.getModules().contains(module));
		Assert.assertTrue(moduleRegistry.getFilters().contains(filter));
		Assert.assertTrue(moduleRegistry.getExceptionMapperLookup().getExceptionMappers().contains(exceptionMapper));
	}

	@Test
	public void setDefaultServiceUrlProvider() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		boot.setDefaultServiceUrlProvider(serviceUrlProvider);
		boot.boot();
		Assert.assertEquals(serviceUrlProvider, boot.getResourceRegistry().getServiceUrlProvider());
	}

	@Test
	public void setServiceUrlProvider() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		boot.setServiceUrlProvider(serviceUrlProvider);
		boot.boot();
		Assert.assertEquals(serviceUrlProvider, boot.getResourceRegistry().getServiceUrlProvider());
	}

	@Test
	public void setConstantServiceUrlProvider() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		final Properties properties = new Properties();
		properties.put(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://something");
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				return (String) properties.get(key);
			}
		};
		boot.setPropertiesProvider(propertiesProvider);
		boot.boot();

		ServiceUrlProvider serviceUrlProvider = boot.getResourceRegistry().getServiceUrlProvider();
		Assert.assertTrue(serviceUrlProvider instanceof ConstantServiceUrlProvider);
		Assert.assertEquals("http://something", serviceUrlProvider.getUrl());
	}

	@Test(expected = IllegalStateException.class)
	public void testReconfigurationProtection() {
		KatharsisBoot boot = new KatharsisBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.boot();
		boot.setObjectMapper(null);
	}

	@Test
	public void boot() {
		KatharsisBoot boot = new KatharsisBoot();
		ObjectMapper objectMapper = boot.getObjectMapper();
		ResourceFieldNameTransformer resourceFieldNameTransformer = new ResourceFieldNameTransformer(
				objectMapper.getSerializationConfig());

		final Properties properties = new Properties();
		properties.put(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.resource.mock");
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
		RegistryEntry taskEntry = resourceRegistry.findEntry(Task.class);
		Assert.assertNotEquals(0, taskEntry.getRelationshipEntries().size());
		ResourceRepositoryAdapter<?, ?> repositoryAdapter = taskEntry.getResourceRepository(null);
		Assert.assertNotNull(repositoryAdapter.getResourceRepository());
		JsonApiResponse response = repositoryAdapter.findAll(new QueryParamsAdapter(new QueryParams()));
		Assert.assertNotNull(response);

		Assert.assertNotNull(requestDispatcher);

		ServiceDiscovery serviceDiscovery = boot.getServiceDiscovery();
		Assert.assertNotNull(serviceDiscovery);
		Assert.assertNotNull(boot.getModuleRegistry());
		Assert.assertNotNull(boot.getExceptionMapperRegistry());

		List<Module> modules = boot.getModuleRegistry().getModules();
		Assert.assertEquals(2, modules.size());
		boot.setDefaultPageLimit(20L);
		boot.setMaxPageLimit(100L);
	}
}
