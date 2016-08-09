package io.katharsis.module;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.Module;

import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.dispatcher.filter.TestFilter;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.ExceptionMapperLookup;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.errorhandling.mapper.KatharsisExceptionMapper;
import io.katharsis.module.Module.ModuleContext;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;

public class SimpleModuleTest {

	private TestModuleContext context;
	private SimpleModule module;

	@Before
	public void setup() {
		context = new TestModuleContext();
		module = new SimpleModule("simple");
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
		Assert.assertEquals(0, context.numResourceRepositoreis);
		Assert.assertEquals(0, context.numRelationshipRepositories);
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
		Assert.assertEquals(0, context.numResourceRepositoreis);
		Assert.assertEquals(0, context.numRelationshipRepositories);
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
		Assert.assertEquals(0, context.numResourceRepositoreis);
		Assert.assertEquals(0, context.numRelationshipRepositories);
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
		Assert.assertEquals(0, context.numResourceRepositoreis);
		Assert.assertEquals(0, context.numRelationshipRepositories);
	}

	@Test
	public void testResourceRepository() {
		module.addRepository(TestResource.class, new TestRepository());
		Assert.assertEquals(1, module.getResourceRepositoryRegistrations().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(1, context.numResourceRepositoreis);
		Assert.assertEquals(0, context.numRelationshipRepositories);
	}

	@Test
	public void testRelationshipRepository() {
		module.addRepository(TestResource.class, TestResource.class, new TestRelationshipRepository());
		Assert.assertEquals(1, module.getRelationshipRepositoryRegistrations().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numResourceRepositoreis);
		Assert.assertEquals(1, context.numRelationshipRepositories);
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
		Assert.assertEquals(0, context.numResourceRepositoreis);
		Assert.assertEquals(0, context.numRelationshipRepositories);
		Assert.assertEquals(1, context.numExceptionMapperLookup);
	}
	
	class TestExceptionMapperLookup implements ExceptionMapperLookup	{

		@SuppressWarnings("rawtypes")
		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			return new HashSet<JsonApiExceptionMapper>( Arrays.asList(new KatharsisExceptionMapper()));
		}
	}

	class TestModuleContext implements ModuleContext {

		private int numResourceInformationBuilds = 0;
		private int numResourceLookups = 0;
		private int numJacksonModules = 0;
		private int numResourceRepositoreis = 0;
		private int numRelationshipRepositories = 0;
		private int numFilters = 0;
		private int numExceptionMapperLookup = 0;

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
		public void addRepository(Class<?> resourceClass, ResourceRepository<?, ?> repository) {
			numResourceRepositoreis++;
		}

		@Override
		public void addRepository(Class<?> sourceResourceClass, Class<?> targetResourceClass,
				RelationshipRepository<?, ?, ?, ?> repository) {
			numRelationshipRepositories++;
		}

		@Override
		public void addFilter(Filter filter) {
			numFilters++;
		}

		@Override
		public ResourceRegistry getResourceRegistry() {
			return new ResourceRegistry(null);
		}

		@Override
		public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
			numExceptionMapperLookup++;
		}

		@Override
		public void addExceptionMapper(ExceptionMapper<?> exceptionMapper) {
			numExceptionMapperLookup++;
		}
	}
}
