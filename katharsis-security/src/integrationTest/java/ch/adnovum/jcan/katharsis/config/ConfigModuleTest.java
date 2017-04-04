package ch.adnovum.jcan.katharsis.config;

import java.util.List;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.system.SystemPropertiesConfigurationSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.adnovum.jcan.katharsis.config.internal.ConfigRepository;
import io.katharsis.internal.boot.KatharsisBoot;
import io.katharsis.module.ServiceDiscovery;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.RegistryEntry;

public class ConfigModuleTest {

	private ConfigModule module;

	private ConfigRepository repo;

	private ConfigurationProvider provider;

	@Before
	public void setup() {
		System.setProperty("adnovum.jcan.test.1", "a");
		System.setProperty("adnovum.jcan.test.2", "b");
		System.setProperty("adnovum.jcan.test.3", "c");

		SystemPropertiesConfigurationSource source = new SystemPropertiesConfigurationSource();
		ConfigurationProviderBuilder builder = new ConfigurationProviderBuilder();
		builder.withConfigurationSource(source);
		provider = builder.build();

		module = ConfigModule.newModule(provider, source);

		// TODO remo simplify a bit...
		KatharsisBoot boot = new KatharsisBoot();
		boot.addModule(module);
		boot.setObjectMapper(new ObjectMapper());
		boot.setResourceFieldNameTransformer(new ResourceFieldNameTransformer());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("test"));
		boot.setServiceDiscovery(Mockito.mock(ServiceDiscovery.class));
		boot.boot();

		RegistryEntry<ConfigEntry> entry = boot.getResourceRegistry().getEntry(ConfigEntry.class);
		repo = (ConfigRepository) entry.getResourceRepository(null).getResourceRepository();
	}

	@Test
	public void findAll() {
		QuerySpec querySpec = new QuerySpec(ConfigEntry.class);
		List<ConfigEntry> list = repo.findAll(querySpec);
		Assert.assertTrue(list.size() > 4);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void addExclusionRule() {
		module.addExclusionRule("jcan");

		QuerySpec querySpec = new QuerySpec(ConfigEntry.class);
		repo.findOne("adnovum.jcan.test.1", querySpec);
	}

	@Test
	public void addInclusionRule() {
		module.addInclusionRule("jcan");

		QuerySpec querySpec = new QuerySpec(ConfigEntry.class);
		List<ConfigEntry> list = repo.findAll(querySpec);
		Assert.assertEquals(3, list.size());
	}

	@Test
	public void addExclusionAndInclusionRule() {
		module.addExclusionRule(".1");
		module.addExclusionRule(".2");
		module.addInclusionRule("jcan");

		QuerySpec querySpec = new QuerySpec(ConfigEntry.class);
		List<ConfigEntry> list = repo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		ConfigEntry entry = list.get(0);
		Assert.assertEquals("adnovum.jcan.test.3", entry.getKey());
	}

	@Test
	public void findUpdate() {
		QuerySpec querySpec = new QuerySpec(ConfigEntry.class);
		ConfigEntry entry = repo.findOne("adnovum.jcan.test.1", querySpec);
		Assert.assertEquals("a", entry.getValue());

		entry.setValue("newValue");
		repo.save(entry);
		Assert.assertEquals("newValue", provider.getProperty("adnovum.jcan.test.1", String.class));
	}

	@After
	public void tearDown() {
		System.clearProperty("adnovum.jcan.test.1");
		System.clearProperty("adnovum.jcan.test.2");
		System.clearProperty("adnovum.jcan.test.3");
	}
}
