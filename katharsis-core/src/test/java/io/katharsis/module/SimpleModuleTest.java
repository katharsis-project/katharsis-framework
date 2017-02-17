package io.katharsis.module;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.Module;

import io.katharsis.core.internal.dispatcher.filter.TestFilter;
import io.katharsis.core.internal.dispatcher.filter.TestRepositoryDecorator;
import io.katharsis.core.internal.exception.ExceptionMapperLookup;
import io.katharsis.core.internal.exception.ExceptionMapperRegistryTest.IllegalStateExceptionMapper;
import io.katharsis.core.internal.exception.KatharsisExceptionMapper;
import io.katharsis.core.internal.registry.ResourceRegistryImpl;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.module.Module.ModuleContext;
import io.katharsis.repository.decorate.RepositoryDecoratorFactory;
import io.katharsis.repository.filter.DocumentFilter;
import io.katharsis.repository.filter.RepositoryFilter;
import io.katharsis.repository.information.RepositoryInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.security.SecurityProvider;
import io.katharsis.utils.parser.TypeParser;

public class SimpleModuleTest {

	private TestModuleContext context;

	private SimpleModule module;

	@Before
	public void setup() {
		context = new TestModuleContext();
		module = new SimpleModule("simple");
	}

	@Test
	public void testGetModuleName() {
		Assert.assertEquals("simple", module.getModuleName());
	}

	@Test
	public void testResourceInformationBuilder() {
		module.addResourceInformationBuilder(new TestResourceInformationBuilder());
		Assert.assertEquals(1, module.getResourceInformationBuilders().size());
		module.setupModule(context);

		Assert.assertEquals(1, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testRepositoryInformationBuilder() {
		module.addRepositoryInformationBuilder(Mockito.mock(RepositoryInformationBuilder.class));
		Assert.assertEquals(1, module.getRepositoryInformationBuilders().size());
		module.setupModule(context);

		Assert.assertEquals(1, context.numRepositoryInformationBuilds);
		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testResourceLookup() {
		module.addResourceLookup(new TestResourceLookup());
		Assert.assertEquals(1, module.getResourceLookups().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(1, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testFilter() {
		module.addFilter(new TestFilter());
		Assert.assertEquals(1, module.getFilters().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(1, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testRepositoryDecorator() {
		module.addRepositoryDecoratorFactory(new TestRepositoryDecorator());
		Assert.assertEquals(1, module.getRepositoryDecoratorFactories().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(1, context.numDecorators);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testJacksonModule() {
		module.addJacksonModule(new com.fasterxml.jackson.databind.module.SimpleModule() {

			private static final long serialVersionUID = 7829254359521781942L;

			@Override
			public String getModuleName() {
				return "test";
			}
		});
		Assert.assertEquals(1, module.getJacksonModules().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(1, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testAddRepository() {
		TestRelationshipRepository repository = new TestRelationshipRepository();
		module.addRepository(repository);
		Assert.assertEquals(1, module.getRepositories().size());

		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(1, context.numRepositories);
		Assert.assertEquals(0, context.numExceptionMapperLookup);
	}

	@Test
	public void testExceptionMapperLookup() {
		module.addExceptionMapperLookup(new TestExceptionMapperLookup());
		Assert.assertEquals(1, module.getExceptionMapperLookups().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
		Assert.assertEquals(1, context.numExceptionMapperLookup);
	}

	@Test
	public void testAddExceptionMapper() {
		module.addExceptionMapper(new IllegalStateExceptionMapper());

		Assert.assertEquals(1, module.getExceptionMapperLookups().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
		Assert.assertEquals(1, context.numExceptionMapperLookup);
	}

	class TestExceptionMapperLookup implements ExceptionMapperLookup {

		@SuppressWarnings("rawtypes")
		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			return new HashSet<JsonApiExceptionMapper>(Arrays.asList(new KatharsisExceptionMapper()));
		}
	}

	class TestModuleContext implements ModuleContext {

		private int numResourceInformationBuilds = 0;

		private int numRepositoryInformationBuilds = 0;

		private int numResourceLookups = 0;

		private int numJacksonModules = 0;

		private int numRepositories = 0;

		private int numFilters = 0;

		private int numExceptionMapperLookup = 0;

		private int numSecurityProviders = 0;

		private int numDecorators = 0;

		@Override
		public void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder) {
			numResourceInformationBuilds++;
		}

		@Override
		public void addResourceLookup(ResourceLookup resourceLookup) {
			numResourceLookups++;
		}

		@Override
		public void addJacksonModule(Module module) {
			numJacksonModules++;
		}

		@Override
		public void addRepository(Class<?> resourceClass, Object repository) {
			numRepositories++;
		}

		@Override
		public void addRepository(Class<?> sourceResourceClass, Class<?> targetResourceClass, Object repository) {
			numRepositories++;
		}

		@Override
		public void addFilter(DocumentFilter filter) {
			numFilters++;
		}

		@Override
		public ResourceRegistry getResourceRegistry() {
			return new ResourceRegistryImpl(null, null);
		}

		@Override
		public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
			numExceptionMapperLookup++;
		}

		@Override
		public void addExceptionMapper(ExceptionMapper<?> exceptionMapper) {
			numExceptionMapperLookup++;
		}

		@Override
		public void addSecurityProvider(SecurityProvider securityProvider) {
			numSecurityProviders++;
		}

		@Override
		public SecurityProvider getSecurityProvider() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ServiceDiscovery getServiceDiscovery() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addRepositoryFilter(RepositoryFilter filter) {
			numFilters++;
		}

		@Override
		public void addRepositoryInformationBuilder(RepositoryInformationBuilder repositoryInformationBuilder) {
			numRepositoryInformationBuilds++;
		}

		@Override
		public void addRepository(Object repository) {
			numRepositories++;
		}

		@Override
		public void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decorator) {
			numDecorators++;
		}

		@Override
		public boolean isServer() {
			return true;
		}

		@Override
		public TypeParser getTypeParser() {
			return null;
		}
	}
}
